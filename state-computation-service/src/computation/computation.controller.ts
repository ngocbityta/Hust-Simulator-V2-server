import { Controller, Logger, Inject } from '@nestjs/common';
import { GrpcMethod } from '@nestjs/microservices';
import { PlayerService } from '../player/player.service';
import { ISpatialService } from '../spatial/spatial.interface';
import { RedisService } from '../redis/redis.service';
import { GrpcContextClient } from '../grpc/context.client';
import { UserActivityState } from '../common/enums/user-activity-state.enum';

interface UserMoveEvent {
  userId: string;
  position: { latitude: number; longitude: number };
  speed: number;
  heading: number;
  clientTimestamp?: number;
}

interface UserStateChangeEvent {
  userId: string;
  activityState: UserActivityState;
  mapId: string;
  eventId: string;
  sessionData: string;
  position?: { latitude: number; longitude: number };
}

@Controller()
export class ComputationController {
  private readonly logger = new Logger(ComputationController.name);

  constructor(
    private readonly playerService: PlayerService,
    @Inject(ISpatialService) private readonly spatialService: ISpatialService,
    private readonly redisService: RedisService,
    private readonly grpcClient: GrpcContextClient,
  ) {}

  @GrpcMethod('ComputationService', 'ProcessUserMove')
  async processUserMove(data: UserMoveEvent) {
    this.logger.debug(`Processing move for user ${data.userId}`);

    await this.playerService.updatePosition(
      data.userId,
      data.position,
      data.speed,
      data.heading,
    );

    // Disseminate to Redis Pub/Sub
    await this.disseminate(data.userId, data.position, {
      position: data.position,
      speed: data.speed,
      heading: data.heading,
      type: 'move',
      clientTimestamp: data.clientTimestamp,
    });

    // 20% random context sync
    if (Math.random() < 0.2) {
      const state = await this.playerService.getPlayerState(data.userId);
      if (state) {
        await this.syncStateWithContext(data.userId, state);
      }
    }

    return { success: true, message: 'OK' };
  }

  @GrpcMethod('ComputationService', 'ProcessUserStateChange')
  async processUserStateChange(data: UserStateChangeEvent) {
    this.logger.debug(`Processing state change for user ${data.userId}`);

    const sessionDataObj = data.sessionData
      ? JSON.parse(data.sessionData)
      : undefined;

    const result = await this.playerService.updateActivityState(
      data.userId,
      data.activityState,
      data.mapId,
      data.eventId,
      sessionDataObj,
    );

    if (result.success) {
      let disseminationPosition = data.position;
      const updatedState = await this.playerService.getPlayerState(data.userId);

      if (!disseminationPosition && updatedState) {
        disseminationPosition = updatedState.position;
      }

      if (disseminationPosition) {
        await this.disseminate(data.userId, disseminationPosition, {
          activityState: data.activityState,
          mapId: data.mapId,
          eventId: data.eventId,
          type: 'state_change',
        });
      }

      if (updatedState) {
        await this.syncStateWithContext(data.userId, updatedState);
      }
    }

    return { success: result.success, message: result.message };
  }

  private async disseminate(
    userId: string,
    position: { latitude: number; longitude: number },
    payload: any,
  ) {
    const currentCell = this.spatialService.getGridCell(
      position.latitude,
      position.longitude,
    );
    const channel = this.spatialService.getCellChannel(currentCell);
    const message = JSON.stringify({
      userId,
      ...payload,
      timestamp: Date.now(),
    });
    await this.redisService.pubClient.publish(channel, message);
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
      this.logger.error(
        `Failed to sync state for user ${userId}: ${err.message}`,
      );
    }
  }
}
