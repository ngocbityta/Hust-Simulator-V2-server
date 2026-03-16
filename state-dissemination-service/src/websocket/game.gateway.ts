import {
    WebSocketGateway,
    WebSocketServer,
    OnGatewayInit,
    OnGatewayConnection,
    OnGatewayDisconnect,
    SubscribeMessage,
    MessageBody,
    ConnectedSocket,
} from '@nestjs/websockets';
import { Logger, Inject } from '@nestjs/common';
import { Server, WebSocket } from 'ws';
import { PlayerService } from '../player/player.service';
import { UserActivityState } from '../common/enums/user-activity-state.enum';
import { DisseminationService } from './dissemination.service';
import { ISessionService } from './session.interface';
import { GrpcContextClient } from '../grpc/context.client';
import { WsEvent } from '../common/enums/ws-event.enum';

@WebSocketGateway({
    cors: { origin: '*' },
    path: '/ws',
})
export class GameGateway
    implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect {
    private readonly logger = new Logger(GameGateway.name);

    @WebSocketServer()
    server!: Server;

    // Sessions are now tracked in SessionService
    
    constructor(
        private readonly playerService: PlayerService,
        private readonly disseminationService: DisseminationService,
        @Inject(ISessionService) private readonly sessionService: ISessionService,
        private readonly grpcClient: GrpcContextClient,
    ) { }

    afterInit() {
        this.logger.log('WebSocket Gateway initialized');
    }

    handleConnection(client: WebSocket) {
        this.logger.debug(`Client connected. Total sessions: ${this.sessionService.getSessionCount() + 1}`);
    }

    handleDisconnect(client: WebSocket) {
        const userId = this.sessionService.getUserId(client);
        this.disseminationService.removeClient(client);
        if (userId) {
            this.sessionService.removeSession(client);
            this.logger.debug(`User ${userId} disconnected. Total sessions: ${this.sessionService.getSessionCount()}`);
        }
    }

    @SubscribeMessage(WsEvent.USER_JOIN)
    async handleUserJoin(
        @ConnectedSocket() client: WebSocket,
        @MessageBody() data: { userId: string },
    ) {
        this.sessionService.setSession(client, data.userId);
        this.logger.log(`User ${data.userId} joined`);

        const playerState = await this.playerService.getPlayerState(data.userId);
        if (playerState) {
            await this.syncStateWithContext(data.userId, playerState);
        }

        return { event: WsEvent.USER_JOINED, data: { userId: data.userId } };
    }

    @SubscribeMessage(WsEvent.USER_MOVE)
    async handleUserMove(
        @ConnectedSocket() client: WebSocket,
        @MessageBody()
        data: {
            position: { latitude: number; longitude: number };
            speed: number;
            heading: number;
        },
    ) {
        const userId = this.sessionService.getUserId(client);
        if (!userId) {
            this.logger.warn(`Move attempt from unauthenticated client.`);
            return { event: WsEvent.USER_ERROR, data: { message: 'Unauthorized' } };
        }
        this.logger.debug(`Processing move for user ${userId}`);

        await this.playerService.updatePosition(
            userId,
            data.position,
            data.speed,
            data.heading,
        );

        await this.disseminationService.updateLocation(
            client,
            userId,
            data.position.latitude,
            data.position.longitude,
            {
                position: data.position,
                speed: data.speed,
                heading: data.heading,
                type: 'move',
                clientTimestamp: (data as any).clientTimestamp,
            }
        );

        if (Math.random() < 0.2) { 
             this.playerService.getPlayerState(userId).then(state => {
                 if (state) this.syncStateWithContext(userId, state);
             });
        }

        return { event: WsEvent.USER_MOVED_ACK, data: { timestamp: Date.now() } };
    }

    @SubscribeMessage(WsEvent.USER_STATE_CHANGE)
    async handleUserStateChange(
        @ConnectedSocket() client: WebSocket,
        @MessageBody()
        data: {
            activityState: UserActivityState;
            mapId?: string;
            eventId?: string;
            sessionData?: Record<string, unknown>;
            position?: { latitude: number; longitude: number };
        },
    ) {
        const userId = this.sessionService.getUserId(client);
        if (!userId) {
            return { event: WsEvent.USER_ERROR, data: { message: 'Unauthorized' } };
        }

        const validStates = Object.values(UserActivityState);
        if (!validStates.includes(data.activityState)) {
            this.logger.warn(`Invalid activity state: ${data.activityState}`);
            return { event: WsEvent.USER_STATE_ERROR, data: { message: 'Invalid activity state' } };
        }

        const result = await this.playerService.updateActivityState(
            userId,
            data.activityState,
            data.mapId,
            data.eventId,
            data.sessionData,
        );

        let disseminationPosition = data.position;
        if (result.success && !disseminationPosition) {
            const playerState = await this.playerService.getPlayerState(userId);
            if (playerState) {
                disseminationPosition = playerState.position;
            }
        }

        if (result.success && disseminationPosition) {
            await this.disseminationService.updateLocation(
                client,
                userId,
                disseminationPosition.latitude,
                disseminationPosition.longitude,
                {
                    activityState: data.activityState,
                    mapId: data.mapId,
                    type: 'state_change',
                }
            );

            const updatedState = await this.playerService.getPlayerState(userId);
            if (updatedState) {
                await this.syncStateWithContext(userId, updatedState);
            }
        }

        return { event: WsEvent.USER_STATE_CHANGED_ACK, data: result };
    }

    private async syncStateWithContext(userId: string, state: any) {
        try {
            await this.grpcClient.updatePlayerState({
                playerId: userId,
                position: state.position,
                activityState: state.activityState || UserActivityState.ROAMING,
                mapId: state.currentMapId || '',
                eventId: state.currentEventId || '',
                timestamp: { millis: Date.now() },
            });
            this.logger.debug(`Synced state for user ${userId} to context-service`);
        } catch (err) {
            this.logger.error(`Failed to sync state for user ${userId}: ${err.message}`);
        }
    }
}
