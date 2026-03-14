import { Controller, Logger } from '@nestjs/common';
import { GrpcMethod } from '@nestjs/microservices';
import { PlayerService } from '../player/player.service';

// gRPC server handler - exposes UserStateService to context-service

@Controller()
export class UserStateGrpcController {
    private readonly logger = new Logger(UserStateGrpcController.name);

    constructor(private readonly playerService: PlayerService) { }

    @GrpcMethod('UserStateService', 'GetNearbyUsers')
    getNearbyUsers(request: {
        userId: string;
        position: { latitude: number; longitude: number };
        radius: number;
    }) {
        this.logger.debug(
            `GetNearbyUsers called for user ${request.userId}`,
        );
        return this.playerService.getNearbyPlayers(
            request.userId,
            request.position,
            request.radius,
        );
    }

    @GrpcMethod('UserStateService', 'NotifyUserConnection')
    notifyUserConnection(request: {
        userId: string;
        isConnected: boolean;
        timestamp: { millis: number };
    }) {
        this.logger.debug(
            `UserConnection: ${request.userId} - ${request.isConnected ? 'connected' : 'disconnected'}`,
        );
        return this.playerService.handleConnectionEvent(request);
    }
}
