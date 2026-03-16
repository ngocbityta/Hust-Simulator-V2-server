import { Controller, Logger } from '@nestjs/common';
import { GrpcMethod } from '@nestjs/microservices';
import { PlayerService } from '../player/player.service';
import { GrpcService, GrpcMethodName } from '../common/enums/grpc.enum';

@Controller()
export class UserStateGrpcController {
    private readonly logger = new Logger(UserStateGrpcController.name);

    constructor(private readonly playerService: PlayerService) { }

    @GrpcMethod(GrpcService.USER_STATE_SERVICE, GrpcMethodName.GET_NEARBY_USERS)
    async getNearbyUsers(request: {
        userId: string;
        position: { latitude: number; longitude: number };
        radius: number;
    }) {
        this.logger.debug(
            `GetNearbyUsers called for user ${request.userId}`,
        );
        return await this.playerService.getNearbyPlayers(
            request.userId,
            request.position,
            request.radius,
        );
    }

    @GrpcMethod(GrpcService.USER_STATE_SERVICE, GrpcMethodName.NOTIFY_USER_CONNECTION)
    async notifyUserConnection(request: {
        userId: string;
        isConnected: boolean;
        timestamp: { millis: number };
    }) {
        this.logger.debug(
            `UserConnection: ${request.userId} - ${request.isConnected ? 'connected' : 'disconnected'}`,
        );
        return await this.playerService.handleConnectionEvent(request);
    }
}
