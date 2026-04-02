import { Injectable, Logger } from '@nestjs/common';
import { RedisService } from '../redis/redis.service';
import { UserActivityState } from '../common/enums/user-activity-state.enum';
import { RedisKey } from '../common/enums/redis-key.enum';

export interface UserState {
  userId: string;
  username?: string;
  avatar?: string;
  position: { latitude: number; longitude: number };
  speed: number;
  heading: number;
  isOnline: boolean;
  activityState: UserActivityState;
  currentMapId?: string;
  currentEventId?: string;
  sessionData?: Record<string, unknown>;
  lastUpdate: number;
}

@Injectable()
export class PlayerService {
  private readonly logger = new Logger(PlayerService.name);

  constructor(private readonly redisService: RedisService) {}

  async updatePosition(
    userId: string,
    position: { latitude: number; longitude: number },
    speed: number,
    heading: number,
  ): Promise<void> {
    const stateKey = `${RedisKey.PLAYER_STATE_PREFIX}${userId}`;

    // Flattened field-value pairs for HASH
    const updates = [
      'userId',
      userId,
      'latitude',
      position.latitude.toString(),
      'longitude',
      position.longitude.toString(),
      'speed',
      speed.toString(),
      'heading',
      heading.toString(),
      'isOnline',
      'true',
    ];

    await this.redisService.client.updatePlayerState(
      stateKey,
      RedisKey.PLAYER_GEO_KEY,
      updates,
      Date.now().toString(),
      'true',
      position.longitude,
      position.latitude,
      userId,
    );
  }

  async getPlayerState(userId: string): Promise<UserState | null> {
    const hash = await this.redisService.client.hgetall(
      `${RedisKey.PLAYER_STATE_PREFIX}${userId}`,
    );
    return this.mapRedisHashToUserState(userId, hash);
  }

  async updateActivityState(
    userId: string,
    state: UserActivityState,
    mapId?: string,
    eventId?: string,
    sessionData?: Record<string, unknown>,
  ): Promise<{ success: boolean; message: string }> {
    const stateKey = `${RedisKey.PLAYER_STATE_PREFIX}${userId}`;
    const exists = await this.redisService.client.exists(stateKey);
    if (!exists) {
      this.logger.warn(
        `Cannot update activity state: user ${userId} not found`,
      );
      return { success: false, message: 'User not found' };
    }

    const updates: (string | number)[] = [
      'activityState',
      state,
      'currentMapId',
      mapId || '',
      'currentEventId',
      eventId || '',
    ];

    if (sessionData) {
      updates.push('sessionData', JSON.stringify(sessionData));
    }

    await this.redisService.client.updatePlayerState(
      stateKey,
      RedisKey.PLAYER_GEO_KEY,
      updates,
      Date.now().toString(),
      'false',
      0,
      0,
      '',
    );

    this.logger.log(`User ${userId} state changed to ${state}`);
    return { success: true, message: 'OK' };
  }

  async getNearbyPlayers(
    userId: string,
    position: { latitude: number; longitude: number },
    radius: number,
  ) {
    const client = this.redisService.client;

    const nearbyIds = (await client.geosearch(
      RedisKey.PLAYER_GEO_KEY,
      'FROMLONLAT',
      position.longitude,
      position.latitude,
      'BYRADIUS',
      radius,
      'm',
    )) as string[];

    const otherIds = nearbyIds.filter((id) => id !== userId);
    if (otherIds.length === 0) {
      return { players: [] };
    }

    const pipeline = client.pipeline();
    otherIds.forEach((id) => {
      pipeline.hgetall(`${RedisKey.PLAYER_STATE_PREFIX}${id}`);
    });

    const results = await pipeline.exec();
    const nearby: any[] = [];

    results?.forEach((res, index) => {
      const [err, hash] = res;
      if (!err && hash && Object.keys(hash as object).length > 0) {
        const uId = otherIds[index];
        const p = this.mapRedisHashToUserState(
          uId,
          hash as Record<string, string>,
        );
        if (p && p.isOnline) {
          nearby.push({
            userId: p.userId,
            username: p.username || '',
            avatar: p.avatar || '',
            position: p.position,
            isOnline: p.isOnline,
            activityState: p.activityState,
          });
        }
      }
    });

    return { players: nearby };
  }

  async handleConnectionEvent(event: {
    userId: string;
    isConnected: boolean;
    timestamp: { millis: number };
  }) {
    const stateKey = `${RedisKey.PLAYER_STATE_PREFIX}${event.userId}`;

    await this.redisService.client.updatePlayerState(
      stateKey,
      RedisKey.PLAYER_GEO_KEY,
      ['isOnline', event.isConnected.toString()],
      Date.now().toString(),
      'false',
      0,
      0,
      '',
    );

    this.logger.log(
      `User ${event.userId} is now ${event.isConnected ? 'online' : 'offline'}`,
    );

    return { success: true, message: 'OK' };
  }

  private mapRedisHashToUserState(
    userId: string,
    hash: Record<string, string>,
  ): UserState | null {
    if (!hash || Object.keys(hash).length === 0) return null;

    return {
      userId,
      username: hash.username,
      avatar: hash.avatar,
      position: {
        latitude: parseFloat(hash.latitude || '0'),
        longitude: parseFloat(hash.longitude || '0'),
      },
      speed: parseFloat(hash.speed || '0'),
      heading: parseFloat(hash.heading || '0'),
      isOnline: hash.isOnline === 'true',
      activityState: hash.activityState as UserActivityState,
      currentMapId: hash.currentMapId,
      currentEventId: hash.currentEventId,
      sessionData: hash.sessionData ? JSON.parse(hash.sessionData) : undefined,
      lastUpdate: parseInt(hash.lastUpdate || '0', 10),
    };
  }
}
