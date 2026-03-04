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
import { PlayerService, ActivityState } from '../player/player.service';

@WebSocketGateway({
    cors: { origin: '*' },
    path: '/ws',
})
export class GameGateway
    implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect {
    private readonly logger = new Logger(GameGateway.name);

    @WebSocketServer()
    server!: Server;

    // Track connected players: WebSocket -> playerId
    private connectedPlayers = new Map<WebSocket, string>();

    constructor(private readonly playerService: PlayerService) { }

    afterInit() {
        this.logger.log('WebSocket Gateway initialized');
    }

    handleConnection(client: WebSocket) {
        this.logger.debug(`Client connected. Total: ${this.connectedPlayers.size + 1}`);
    }

    handleDisconnect(client: WebSocket) {
        const playerId = this.connectedPlayers.get(client);
        if (playerId) {
            this.connectedPlayers.delete(client);
            this.logger.debug(`Player ${playerId} disconnected. Total: ${this.connectedPlayers.size}`);
        }
    }

    @SubscribeMessage('player:join')
    handlePlayerJoin(
        @ConnectedSocket() client: WebSocket,
        @MessageBody() data: { playerId: string },
    ) {
        this.connectedPlayers.set(client, data.playerId);
        this.logger.log(`Player ${data.playerId} joined`);
        return { event: 'player:joined', data: { playerId: data.playerId } };
    }

    @SubscribeMessage('player:move')
    handlePlayerMove(
        @ConnectedSocket() client: WebSocket,
        @MessageBody()
        data: {
            playerId: string;
            position: { latitude: number; longitude: number };
            speed: number;
            heading: number;
        },
    ) {
        // TODO: Validate movement, update player state, broadcast to nearby players
        this.logger.debug(`Player ${data.playerId} moved to (${data.position.latitude}, ${data.position.longitude})`);

        return { event: 'player:moved', data };
    }

    @SubscribeMessage('player:state_change')
    handlePlayerStateChange(
        @ConnectedSocket() client: WebSocket,
        @MessageBody()
        data: {
            playerId: string;
            activityState: ActivityState;
            zoneId?: string;
            sessionData?: Record<string, unknown>;
        },
    ) {
        const validStates = Object.values(ActivityState);
        if (!validStates.includes(data.activityState)) {
            this.logger.warn(`Invalid activity state: ${data.activityState}`);
            return { event: 'player:state_error', data: { message: 'Invalid activity state' } };
        }

        const result = this.playerService.updateActivityState(
            data.playerId,
            data.activityState,
            data.zoneId,
            data.sessionData,
        );

        if (result.success) {
            // Broadcast state change to nearby players
            this.server.clients.forEach((wsClient: WebSocket) => {
                if (wsClient !== client && wsClient.readyState === WebSocket.OPEN) {
                    wsClient.send(JSON.stringify({
                        event: 'player:state_changed',
                        data: {
                            playerId: data.playerId,
                            activityState: data.activityState,
                        },
                    }));
                }
            });
        }

        return { event: 'player:state_changed', data: result };
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
