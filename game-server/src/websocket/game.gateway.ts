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

    constructor(private readonly playerService: PlayerService) { }

    afterInit() {
        this.logger.log('WebSocket Gateway initialized');
    }

    handleConnection(client: WebSocket) {
        this.logger.debug(`Client connected. Total: ${this.connectedPlayers.size + 1}`);
    }

    handleDisconnect(client: WebSocket) {
        const userId = this.connectedPlayers.get(client);
        if (userId) {
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
    handleUserMove(
        @ConnectedSocket() client: WebSocket,
        @MessageBody()
        data: {
            userId: string;
            position: { latitude: number; longitude: number };
            speed: number;
            heading: number;
        },
    ) {
        // TODO: Validate movement, update user state, broadcast to nearby users
        this.logger.debug(`User ${data.userId} moved to (${data.position.latitude}, ${data.position.longitude})`);

        return { event: 'user:moved', data };
    }

    @SubscribeMessage('user:state_change')
    handleUserStateChange(
        @ConnectedSocket() client: WebSocket,
        @MessageBody()
        data: {
            userId: string;
            activityState: UserActivityState;
            mapId?: string;
            eventId?: string;
            sessionData?: Record<string, unknown>;
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

        if (result.success) {
            // Broadcast state change to nearby users
            this.server.clients.forEach((wsClient: WebSocket) => {
                if (wsClient !== client && wsClient.readyState === WebSocket.OPEN) {
                    wsClient.send(JSON.stringify({
                        event: 'user:state_changed',
                        data: {
                            userId: data.userId,
                            activityState: data.activityState,
                        },
                    }));
                }
            });
        }

        return { event: 'user:state_changed', data: result };
    }

    // Broadcast delta snapshot to specific clients
    broadcastToNearby(
        position: { latitude: number; longitude: number },
        radius: number,
        event: string,
        data: unknown,
    ) {
        // TODO: Implement spatial filtering - only send to players within radius
        this.server.clients.forEach((client: WebSocket) => {
            if (client.readyState === WebSocket.OPEN) {
                client.send(JSON.stringify({ event, data }));
            }
        });
    }
}
