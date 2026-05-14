import { Injectable, Logger } from '@nestjs/common';
import { RedisService } from '../redis/redis.service';
import { TrajectoryPoint } from '../grpc/prediction.client';

@Injectable()
export class TrajectoryService {
  private readonly logger = new Logger(TrajectoryService.name);
  private readonly maxTrajectoryLength = 20;

  constructor(private readonly redisService: RedisService) {}

  async addPoint(userId: string, lat: number, lng: number, timestamp: number) {
    const key = `trajectory:${userId}`;
    const point = JSON.stringify({ latitude: lat, longitude: lng, timestamp });
    
    // Add to list and trim
    await this.redisService.client.lpush(key, point);
    await this.redisService.client.ltrim(key, 0, this.maxTrajectoryLength - 1);
    // Set expiry (e.g., 1 hour)
    await this.redisService.client.expire(key, 3600);
  }

  async getTrajectory(userId: string): Promise<TrajectoryPoint[]> {
    const key = `trajectory:${userId}`;
    const pointsStr = await this.redisService.client.lrange(key, 0, -1);
    
    return pointsStr.map(p => JSON.parse(p) as TrajectoryPoint);
  }
}
