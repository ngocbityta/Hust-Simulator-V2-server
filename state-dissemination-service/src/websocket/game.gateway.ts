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
import { Logger } from '@nestjs/common';
import { Server, WebSocket } from 'ws';
import { PlayerService, UserActivityState } from '../player/player.service';
import { DisseminationService } from './dissemination.service';
import { GrpcContextClient } from '../grpc/context.client';

@WebSocketGateway({
    cors: { origin: '*' },
    path: '/ws',
})
export class GameGateway
    implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect {
    private readonly logger = new Logger(GameGateway.name);

    @WebSocketServer()
    server!: Server;

    // Track connected players: WebSocket -> userId
    private connectedPlayers = new Map<WebSocket, string>();

    constructor(
        private readonly playerService: PlayerService,
        private readonly disseminationService: DisseminationService,
        private readonly grpcClient: GrpcContextClient,
    ) { }

    afterInit() {
        this.logger.log('WebSocket Gateway initialized');
    }

    handleConnection(client: WebSocket) {
        this.logger.debug(`Client connected. Total: ${this.connectedPlayers.size + 1}`);
    }

    handleDisconnect(client: WebSocket) {
        const userId = this.connectedPlayers.get(client);
        if (userId) {
            this.disseminationService.removeClient(client);
            this.connectedPlayers.delete(client);
            this.logger.debug(`User ${userId} disconnected. Total: ${this.connectedPlayers.size}`);
        }
    }

    @SubscribeMessage('user:join')
    handleUserJoin(
        @ConnectedSocket() client: WebSocket,
        @MessageBody() data: { userId: string },
    ) {
        this.connectedPlayers.set(client, data.userId);
        this.logger.log(`User ${data.userId} joined`);
        return { event: 'user:joined', data: { userId: data.userId } };
    }

    @SubscribeMessage('user:move')
    async handleUserMove(
        @ConnectedSocket() client: WebSocket,
        @MessageBody()
        data: {
            userId: string;
            position: { latitude: number; longitude: number };
            speed: number;
            heading: number;
        },
    ) {
        // 1. Update In-memory Player State
        this.playerService.updatePosition(
            data.userId,
            data.position,
            data.speed,
            data.heading,
        );

        // 2. AOI-based State Dissemination via Redis Pub/Sub
        await this.disseminationService.updateLocation(
            client,
            data.userId,
            data.position.latitude,
            data.position.longitude,
            {
                position: data.position,
                speed: data.speed,
                heading: data.heading,
                type: 'move',
            }
        );

        // 3. gRPC: Verification of Context Service connectivity
        // We only do this occasionally or for specific triggers to avoid overhead
        if (Math.random() < 0.1) {
            this.grpcClient.checkPlayerZone(data.userId, data.position.latitude, data.position.longitude)
                .then(res => this.logger.debug(`gRPC Zone Check for ${data.userId}: ${res.zones?.length || 0} zones`))
                .catch(err => this.logger.error(`gRPC Error for ${data.userId}: ${err.message}`));
        }

        return { event: 'user:moved_ack', data: { timestamp: Date.now() } };
    }

    @SubscribeMessage('user:state_change')
    async handleUserStateChange(
        @ConnectedSocket() client: WebSocket,
        @MessageBody()
        data: {
            userId: string;
            activityState: UserActivityState;
            mapId?: string;
            eventId?: string;
            sessionData?: Record<string, unknown>;
            position?: { latitude: number; longitude: number };
        },
    ) {
        const validStates = Object.values(UserActivityState);
        if (!validStates.includes(data.activityState)) {
            this.logger.warn(`Invalid activity state: ${data.activityState}`);
            return { event: 'user:state_error', data: { message: 'Invalid activity state' } };
        }

        const result = this.playerService.updateActivityState(
            data.userId,
            data.activityState,
            data.mapId,
            data.eventId,
            data.sessionData,
        );

        if (result.success && data.position) {
            // Disseminate state change to nearby users
            await this.disseminationService.updateLocation(
                client,
                data.userId,
                data.position.latitude,
                data.position.longitude,
                {
                    activityState: data.activityState,
                    mapId: data.mapId,
                    type: 'state_change',
                }
            );
        }

        return { event: 'user:state_changed_ack', data: result };
    }
}
