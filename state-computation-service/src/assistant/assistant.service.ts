import { Injectable, Logger, Inject } from '@nestjs/common';
import * as turf from '@turf/turf';
import { RedisService } from '../redis/redis.service';
import { HeatmapService } from '../heatmap/heatmap.service';
import { ISpatialService } from '../spatial/spatial.interface';
import { ConfigService } from '@nestjs/config';
import { GrpcContextClient } from '../grpc/context.client';

@Injectable()
export class AssistantService {
  private readonly logger = new Logger(AssistantService.name);

  constructor(
    private readonly redisService: RedisService,
    private readonly heatmapService: HeatmapService,
    @Inject(ISpatialService) private readonly spatialService: ISpatialService,
    private readonly configService: ConfigService,
    private readonly contextClient: GrpcContextClient,
  ) {}

  async generateJourneyContext(
    userId: string,
    currentLat: number,
    currentLng: number,
    targetName: string,
    targetLat: number,
    targetLng: number,
    speed: number, // m/s
  ): Promise<void> {
    const throttleKey = `throttle:assistant:${userId}`;
    const throttleSeconds = this.configService.get<number>('ASSISTANT_NOTIFICATION_THROTTLE_SECONDS', 180);
    // Debounce: Only send 1 notification every N minutes
    const canNotify = await this.redisService.throttle(throttleKey, throttleSeconds);

    if (!canNotify) {
      return;
    }

    try {
      // 1. Calculate ETA
      const distanceMeters = turf.distance(
        turf.point([currentLng, currentLat]),
        turf.point([targetLng, targetLat]),
        { units: 'meters' },
      );

      // Assume walking speed ~ default if speed is 0
      const defaultWalkingSpeed = this.configService.get<number>('ASSISTANT_DEFAULT_WALKING_SPEED', 1.4);
      const effectiveSpeed = speed > 0.5 ? speed : defaultWalkingSpeed;
      const timeSeconds = distanceMeters / effectiveSpeed;
      const etaMinutes = Math.ceil(timeSeconds / 60);

      // 2. Check Density
      let densityMsg = '';
      const targetCell = this.spatialService.getGridCell(targetLat, targetLng);
      const latestHeatmap = this.heatmapService.getLatestHeatmap();

      if (latestHeatmap) {
        const cellData = latestHeatmap.cells.find(
          (c) => c.cellX === targetCell.x && c.cellY === targetCell.y,
        );

        if (cellData) {
          const count = cellData.count;
          const crowdedThreshold = this.configService.get<number>('ASSISTANT_CROWDED_THRESHOLD', 20);
          const sparseThreshold = this.configService.get<number>('ASSISTANT_SPARSE_THRESHOLD', 5);

          // Simple heuristic for "crowdedness" based on cell count
          if (count > crowdedThreshold) {
            densityMsg = `Hiện khu vực này đang rất đông (${count} người).`;
          } else if (count < sparseThreshold) {
            densityMsg = `Khu vực này hiện đang khá vắng.`;
          } else {
            densityMsg = `Mật độ người ở mức bình thường.`;
          }

          // 2.5 Historical Comparison
          try {
            // Compare with last 7 days average
            const sinceMs = Date.now() - (7 * 24 * 60 * 60 * 1000);
            const history = await this.contextClient.getHistoricalDensity(targetCell.x, targetCell.y, sinceMs);
            
            if (history && history.averageCount > 0) {
              const diffPercent = ((count - history.averageCount) / history.averageCount) * 100;
              if (Math.abs(diffPercent) > 30) {
                const trend = diffPercent > 0 ? 'đông hơn' : 'vắng hơn';
                densityMsg += ` (${trend} ${Math.abs(Math.round(diffPercent))}% so với thường lệ).`;
              }
            }
          } catch (err) {
            this.logger.warn(`Failed to fetch historical density for cell ${targetCell.x},${targetCell.y}`);
          }
        } else {
          densityMsg = `Khu vực này hiện đang vắng.`;
        }
      }

      // 3. Construct Message
      const etaMsg = etaMinutes <= 1 ? "Bạn sắp tới" : `Bạn còn khoảng ${etaMinutes} phút tới`;
      const finalMessage = `${etaMsg} ${targetName}. ${densityMsg}`.trim();

      this.logger.log(`Assistant Message for ${userId}: ${finalMessage}`);

      // 4. Publish Notification
      const currentGridCell = this.spatialService.getGridCell(currentLat, currentLng);
      const channel = this.spatialService.getCellChannel(currentGridCell);
      
      const payload = {
        type: 'assistant_notification',
        targetUserId: userId,
        message: finalMessage,
        timestamp: Date.now()
      };

      await this.redisService.pubClient.publish(channel, JSON.stringify(payload));

    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : String(error);
      this.logger.error(`Failed to generate journey context for ${userId}: ${message}`);
    }
  }
}
