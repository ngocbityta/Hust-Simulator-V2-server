import { Injectable, Logger, Inject, OnModuleInit } from '@nestjs/common';
import { SchedulerRegistry } from '@nestjs/schedule';
import { ConfigService } from '@nestjs/config';
import { RedisService } from '../redis/redis.service';
import { ISpatialService } from '../spatial/spatial.interface';
import { RedisKey } from '../common/enums/redis-key.enum';

interface HeatmapCell {
  cellX: number;
  cellY: number;
  count: number;
  centerLat: number;
  centerLng: number;
  activities: Record<string, number>;
}

interface HeatmapPayload {
  timestamp: number;
  totalOnline: number;
  cells: HeatmapCell[];
}

@Injectable()
export class HeatmapService implements OnModuleInit {
  private readonly logger = new Logger(HeatmapService.name);

  constructor(
    private readonly redisService: RedisService,
    @Inject(ISpatialService) private readonly spatialService: ISpatialService,
    private readonly schedulerRegistry: SchedulerRegistry,
    private readonly configService: ConfigService,
  ) {}

  onModuleInit() {
    const intervalMs = this.configService.get<number>(
      'HEATMAP_INTERVAL_MS',
      5000,
    );
    const interval = setInterval(() => {
      this.aggregateHeatmap().catch((err) => {
        this.logger.error('Error during aggregateHeatmap:', err);
      });
    }, intervalMs);
    this.schedulerRegistry.addInterval('heatmapInterval', interval);
    this.logger.log(`Heatmap interval set to ${intervalMs}ms`);
  }

  async aggregateHeatmap(): Promise<void> {
    const startTime = Date.now();
    const cellSize = this.spatialService.getCellSize();
    const metersPerLat = this.spatialService.getMetersPerLat();
    const metersPerLng = this.spatialService.getMetersPerLng();

    try {
      // 1. Get all tracked player IDs from the GeoSet
      const userIds = await this.redisService.client.zrange(
        RedisKey.PLAYER_GEO_KEY,
        0,
        -1,
      );

      if (userIds.length === 0) {
        return;
      }

      // 2. Pipeline: fetch state for each player
      const pipeline = this.redisService.client.pipeline();
      for (const userId of userIds) {
        pipeline.hgetall(`${RedisKey.PLAYER_STATE_PREFIX}${userId}`);
      }
      const results = await pipeline.exec();

      // 3. Filter online players and group by grid cell
      const cellMap = new Map<
        string,
        {
          cellX: number;
          cellY: number;
          count: number;
          activities: Map<string, number>;
        }
      >();
      let totalOnline = 0;

      results?.forEach((res) => {
        const [err, hash] = res;
        if (err || !hash || Object.keys(hash as object).length === 0) return;

        const state = hash as Record<string, string>;
        if (state.isOnline !== 'true') return;

        const lat = parseFloat(state.latitude || '0');
        const lng = parseFloat(state.longitude || '0');
        if (lat === 0 && lng === 0) return;

        totalOnline++;

        const cell = this.spatialService.getGridCell(lat, lng);
        const cellKey = this.spatialService.getCellKey(cell);
        const activity = state.activityState || 'UNKNOWN';

        if (!cellMap.has(cellKey)) {
          cellMap.set(cellKey, {
            cellX: cell.x,
            cellY: cell.y,
            count: 0,
            activities: new Map<string, number>(),
          });
        }

        const cellData = cellMap.get(cellKey)!;
        cellData.count++;
        cellData.activities.set(
          activity,
          (cellData.activities.get(activity) || 0) + 1,
        );
      });

      // 4. Build payload with center coordinates
      const cells: HeatmapCell[] = [];
      for (const cellData of cellMap.values()) {
        const centerLng = ((cellData.cellX + 0.5) * cellSize) / metersPerLng;
        const centerLat = ((cellData.cellY + 0.5) * cellSize) / metersPerLat;

        const activities: Record<string, number> = {};
        for (const [state, count] of cellData.activities) {
          activities[state] = count;
        }

        cells.push({
          cellX: cellData.cellX,
          cellY: cellData.cellY,
          count: cellData.count,
          centerLat,
          centerLng,
          activities,
        });
      }

      // 5. Publish to Redis
      const payload: HeatmapPayload = {
        timestamp: Date.now(),
        totalOnline,
        cells,
      };

      await this.redisService.pubClient.publish(
        RedisKey.HEATMAP_CHANNEL,
        JSON.stringify(payload),
      );

      this.logger.debug(
        `Heatmap published: ${totalOnline} online, ${cells.length} active cells. Took ${Date.now() - startTime}ms`,
      );
    } catch (err) {
      if (err instanceof Error) {
        this.logger.error(`Heatmap aggregation failed: ${err.message}`);
      }
    }
  }
}
