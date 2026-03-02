import { Controller, Logger } from '@nestjs/common';
import { GrpcMethod } from '@nestjs/microservices';
import { PlayerService } from '../player/player.service';

// gRPC server handler - exposes PlayerStateService to context-service

@Controller()
export class PlayerStateGrpcController {
    private readonly logger = new Logger(PlayerStateGrpcController.name);

    constructor(private readonly playerService: PlayerService) { }

    @GrpcMethod('PlayerStateService', 'GetNearbyPlayers')
    getNearbyPlayers(request: {
        playerId: string;
        position: { latitude: number; longitude: number };
        radius: number;
    }) {
        this.logger.debug(
            `GetNearbyPlayers called for player ${request.playerId}`,
        );
        return this.playerService.getNearbyPlayers(
            request.playerId,
            request.position,
            request.radius,
        );
    }

    @GrpcMethod('PlayerStateService', 'NotifyPlayerConnection')
    notifyPlayerConnection(request: {
        playerId: string;
        isConnected: boolean;
        timestamp: { millis: number };
    }) {
        this.logger.debug(
            `PlayerConnection: ${request.playerId} - ${request.isConnected ? 'connected' : 'disconnected'}`,
        );
        return this.playerService.handleConnectionEvent(request);
    }
}
