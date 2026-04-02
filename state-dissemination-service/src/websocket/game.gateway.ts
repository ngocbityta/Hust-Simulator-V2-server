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
import { UserActivityState } from '../common/enums/user-activity-state.enum';
import { DisseminationService } from './dissemination.service';
import { ISessionService } from './session.interface';
import { GrpcComputationClient } from '../grpc/computation.client';
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

    constructor(
        private readonly grpcComputationClient: GrpcComputationClient,
        private readonly disseminationService: DisseminationService,
        @Inject(ISessionService) private readonly sessionService: ISessionService,
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

        // Update AOI logically for this WS connection
        await this.disseminationService.updateLocation(
            client,
            data.position.latitude,
            data.position.longitude
        );

        // Forward computation request to ComputationService
        await this.grpcComputationClient.processUserMove(
            userId,
            data.position,
            data.speed,
            data.heading,
            (data as any).clientTimestamp
        );

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

        // Delegate state updates and context-syncing to Computation Service
        const result = await this.grpcComputationClient.processUserStateChange(
            userId,
            data.activityState,
            data.mapId,
            data.eventId,
            data.sessionData,
            data.position
        );

        return { event: WsEvent.USER_STATE_CHANGED_ACK, data: result };
    }
}
