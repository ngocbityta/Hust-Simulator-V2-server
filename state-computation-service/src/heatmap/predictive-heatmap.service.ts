import { Injectable, Logger, Inject, OnModuleInit } from '@nestjs/common';
import { SchedulerRegistry } from '@nestjs/schedule';
import { ConfigService } from '@nestjs/config';
import { RedisService } from '../redis/redis.service';
import { ISpatialService } from '../spatial/spatial.interface';
import { RedisKey } from '../common/enums/redis-key.enum';
import { IIntentService, IntentPrediction } from '../intent/intent.interface';
import * as fs from 'fs';
import * as path from 'path';

import { PredictiveHeatmapCell, PredictiveHeatmapPayload, ActiveEvent, Waypoint } from '../common/interfaces/heatmap.interface';
import { TRANSIT_GATES, PARKING_AREAS, CANTEEN_AREAS } from '../common/constants/transit.constant';
import { findNearest, interpolatePoints, getCampusPhase, getDistance } from '../common/utils/geo.util';
import { DEFAULT_WEEKDAY_PROFILE } from '../common/constants/activity-profile.constant';
import { calculateActivityMultiplier } from '../common/utils/time.util';

@Injectable()
export class PredictiveHeatmapService implements OnModuleInit {
  private readonly logger = new Logger(PredictiveHeatmapService.name);
  private latestHeatmap: PredictiveHeatmapPayload | null = null;
  private userIntentCache = new Map<string, { prediction: IntentPrediction, timestamp: number, lat: number, lng: number }>();

  // db_uuid -> { name, lat, lng }
  private poisMap = new Map<string, { name: string, lat: number, lng: number }>();

  constructor(
    private readonly redisService: RedisService,
    @Inject(ISpatialService) private readonly spatialService: ISpatialService,
    @Inject(IIntentService) private readonly intentService: IIntentService,
    private readonly schedulerRegistry: SchedulerRegistry,
    private readonly configService: ConfigService,
  ) { }

  onModuleInit() {
    try {
      const poisData = fs.readFileSync(path.join(process.cwd(), 'pois.json'), 'utf-8');
      const rawMap = JSON.parse(poisData);
      for (const key in rawMap) {
        const poi = rawMap[key];
        if (poi.db_uuid) {
          this.poisMap.set(poi.db_uuid, {
            name: poi.name,
            lat: poi.lat,
            lng: poi.lng,
          });
        }
      }
      this.logger.log(`Loaded ${this.poisMap.size} POIs into predictive heatmap service.`);
    } catch (err) {
      this.logger.warn(`Could not load pois.json: ${err}`);
    }

    const intervalMs = this.configService.get<number>(
      'HEATMAP_PREDICTIVE_INTERVAL_MS',
      10000, // Aggregate every 10s by default
    );
    const interval = setInterval(() => {
      this.aggregatePredictiveHeatmap().catch((err) => {
        this.logger.error('Error during aggregatePredictiveHeatmap:', err);
      });
    }, intervalMs);
    this.schedulerRegistry.addInterval('predictiveHeatmapInterval', interval);
    this.logger.log(`Predictive heatmap interval set to ${intervalMs}ms`);
  }

  async aggregatePredictiveHeatmap(): Promise<void> {
    await this.generateHeatmap();
  }

  async generateHeatmap(targetTimestampMs?: number): Promise<PredictiveHeatmapPayload | null> {
    const startTime = Date.now();
    const cellSize = this.spatialService.getCellSize();
    const metersPerLat = this.spatialService.getMetersPerLat();
    const metersPerLng = this.spatialService.getMetersPerLng();
    const CACHE_TTL_MS = this.configService.get<number>('INTENT_CACHE_TTL_MS', 30000);

    const activityMultiplier = this.getActivityMultiplier(targetTimestampMs);
    const { transitRatio, phase } = getCampusPhase(targetTimestampMs);

    try {
      // 1. Get all tracked player IDs from the GeoSet
      const userIds = await this.redisService.client.zrange(
        RedisKey.PLAYER_GEO_KEY,
        0,
        -1,
      );

      if (userIds.length === 0) {
        return null;
      }

      // 2. Pipeline: fetch state for each player to get current location
      const pipeline = this.redisService.client.pipeline();
      for (const userId of userIds) {
        pipeline.hgetall(`${RedisKey.PLAYER_STATE_PREFIX}${userId}`);
      }
      const results = await pipeline.exec();

      // 3. Process predictions for online users
      const cellMap = new Map<
        string,
        {
          cellX: number;
          cellY: number;
          count: number;
          intents: Map<string, number>;
        }
      >();

      const eventMultipliers = new Map<string, number>();
      const phantomDensities: Array<{ lat: number, lng: number, count: number, name: string }> = [];

      if (targetTimestampMs) {
        const activeEvents = await this.fetchActiveEvents(targetTimestampMs);
        for (const ev of activeEvents) {
          if (ev.buildingId && this.poisMap.has(ev.buildingId)) {
            const poi = this.poisMap.get(ev.buildingId);
            if (poi) {
              const participants = ev.estimatedParticipants || 0;
              const multi = 1 + (participants / 20.0);
              eventMultipliers.set(ev.buildingId, multi);

              if (participants > 0) {
                phantomDensities.push({
                  lat: poi.lat,
                  lng: poi.lng,
                  count: participants * activityMultiplier * 0.5,
                  name: `[Event|${poi.name}] ${ev.name}`
                });
              }
            }
          }
        }
        this.logger.debug(`Forecast mode: ${activeEvents.length} active events, ${phantomDensities.length} phantom injections`);
      }

      let totalOnline = 0;
      const predictionPromises = [];

      for (let i = 0; i < userIds.length; i++) {
        const userId = userIds[i];
        const res = results?.[i];

        if (!res) continue;
        const [err, hash] = res;
        if (err || !hash || Object.keys(hash as object).length === 0) continue;

        const state = hash as Record<string, string>;
        if (state.isOnline !== 'true') continue;

        const lat = parseFloat(state.latitude || '0');
        const lng = parseFloat(state.longitude || '0');
        if (lat === 0 && lng === 0) continue;

        totalOnline++;

        // Cache check
        const cacheKey = targetTimestampMs
          ? `${userId}:forecast:${Math.floor(targetTimestampMs / 60000)}`
          : `${userId}:live`;
        const cached = this.userIntentCache.get(cacheKey);
        let predictionPromise: Promise<IntentPrediction | null>;

        if (cached && (startTime - cached.timestamp < CACHE_TTL_MS)) {
          predictionPromise = Promise.resolve(cached.prediction);
        } else {
          predictionPromise = this.intentService.predictIntent(userId, lat, lng, undefined, targetTimestampMs).then(pred => {
            if (pred) {
              this.userIntentCache.set(cacheKey, { prediction: pred, timestamp: Date.now(), lat, lng });
            }
            return pred;
          });
        }

        predictionPromises.push(
          predictionPromise.then(prediction => {
            if (!prediction) return;

            const candidates = prediction.candidateDestinations || [];

            if (candidates.length === 0 && prediction.targetLat && prediction.targetLng) {
              candidates.push({
                poiId: prediction.predictedDestinationId || 'unknown',
                poiName: prediction.predictedDestinationName || prediction.intent || 'UNKNOWN',
                probability: 1.0,
                lat: prediction.targetLat,
                lng: prediction.targetLng
              });
            }

            let totalProb = 0;
            for (const cand of candidates) {
              if (eventMultipliers.has(cand.poiId)) {
                cand.probability *= eventMultipliers.get(cand.poiId)!;
              }
              totalProb += cand.probability;
            }
            if (totalProb > 0) {
              for (const cand of candidates) {
                cand.probability /= totalProb;
              }
            }

            for (const cand of candidates) {
              const scaledProb = cand.probability * activityMultiplier;
              const buildingWeight = scaledProb * (1 - transitRatio);
              const transitWeight = scaledProb * transitRatio;

              // 1. Distribute building weight to the building cell
              if (buildingWeight > 0) {
                const targetCell = this.spatialService.getGridCell(cand.lat, cand.lng);
                const cellKey = this.spatialService.getCellKey(targetCell);

                if (!cellMap.has(cellKey)) {
                  cellMap.set(cellKey, {
                    cellX: targetCell.x,
                    cellY: targetCell.y,
                    count: 0,
                    intents: new Map<string, number>(),
                  });
                }

                const cellData = cellMap.get(cellKey)!;
                cellData.count += buildingWeight;

                const intentStr = cand.poiName || 'UNKNOWN';
                cellData.intents.set(
                  intentStr,
                  (cellData.intents.get(intentStr) || 0) + buildingWeight,
                );
              }

              // 2. Distribute transit weight along path waypoints if transitRatio > 0
              if (transitWeight > 0) {
                let pathPoints: Array<{ lat: number, lng: number }> = [];

                if (phase === 'ARRIVING') {
                  const startNode = findNearest(cand.lat, cand.lng, [...TRANSIT_GATES, ...PARKING_AREAS]);
                  const dist = getDistance(startNode.lat, startNode.lng, cand.lat, cand.lng);
                  const steps = Math.max(2, Math.ceil(dist / 10));
                  pathPoints = interpolatePoints(startNode.lat, startNode.lng, cand.lat, cand.lng, steps);
                } else if (phase === 'DEPARTING') {
                  const endNode = findNearest(cand.lat, cand.lng, [...TRANSIT_GATES, ...PARKING_AREAS]);
                  const dist = getDistance(cand.lat, cand.lng, endNode.lat, endNode.lng);
                  const steps = Math.max(2, Math.ceil(dist / 10));
                  pathPoints = interpolatePoints(cand.lat, cand.lng, endNode.lat, endNode.lng, steps);
                } else if (phase === 'LUNCH_BREAK') {
                  const endNode = findNearest(cand.lat, cand.lng, CANTEEN_AREAS);
                  const dist = getDistance(cand.lat, cand.lng, endNode.lat, endNode.lng);
                  const steps = Math.max(2, Math.ceil(dist / 10));
                  pathPoints = interpolatePoints(cand.lat, cand.lng, endNode.lat, endNode.lng, steps);
                } else {
                  pathPoints = [
                    { lat: cand.lat, lng: cand.lng },
                    { lat: cand.lat + 0.0001, lng: cand.lng + 0.0001 },
                    { lat: cand.lat - 0.0001, lng: cand.lng - 0.0001 }
                  ];
                }

                const weightPerPoint = transitWeight / pathPoints.length;
                for (const pt of pathPoints) {
                  const ptCell = this.spatialService.getGridCell(pt.lat, pt.lng);
                  const ptKey = this.spatialService.getCellKey(ptCell);

                  if (!cellMap.has(ptKey)) {
                    cellMap.set(ptKey, {
                      cellX: ptCell.x,
                      cellY: ptCell.y,
                      count: 0,
                      intents: new Map<string, number>(),
                    });
                  }

                  const ptCellData = cellMap.get(ptKey)!;
                  ptCellData.count += weightPerPoint;

                  const intentStr = cand.poiName || 'UNKNOWN';
                  ptCellData.intents.set(
                    intentStr,
                    (ptCellData.intents.get(intentStr) || 0) + weightPerPoint,
                  );
                }
              }
            }
          })
        );
      }

      await Promise.allSettled(predictionPromises);

      for (const phantom of phantomDensities) {
        const targetCell = this.spatialService.getGridCell(phantom.lat, phantom.lng);
        const cellKey = this.spatialService.getCellKey(targetCell);

        if (!cellMap.has(cellKey)) {
          cellMap.set(cellKey, {
            cellX: targetCell.x,
            cellY: targetCell.y,
            count: 0,
            intents: new Map<string, number>(),
          });
        }

        const cellData = cellMap.get(cellKey)!;
        cellData.count += phantom.count;
        cellData.intents.set(
          phantom.name,
          (cellData.intents.get(phantom.name) || 0) + phantom.count
        );
      }

      // Clean up stale cache
      for (const [key, cacheData] of this.userIntentCache.entries()) {
        if (startTime - cacheData.timestamp > CACHE_TTL_MS) {
          this.userIntentCache.delete(key);
        }
      }

      // 4. Build payload with center coordinates
      const cells: PredictiveHeatmapCell[] = [];
      for (const cellData of cellMap.values()) {
        const centerLng = ((cellData.cellX + 0.5) * cellSize) / metersPerLng;
        const centerLat = ((cellData.cellY + 0.5) * cellSize) / metersPerLat;

        const intents: Record<string, number> = {};
        for (const [intentName, count] of cellData.intents) {
          intents[intentName] = count;
        }

        cells.push({
          cellX: cellData.cellX,
          cellY: cellData.cellY,
          count: cellData.count,
          centerLat,
          centerLng,
          intents,
        });
      }

      // 5. Build Payload
      const payload: PredictiveHeatmapPayload = {
        timestamp: targetTimestampMs || Date.now(),
        totalOnline: Math.round(totalOnline * activityMultiplier),
        cells,
      };

      // 6. Publish to Redis (only for default prediction)
      if (!targetTimestampMs) {
        this.latestHeatmap = payload;

        await this.redisService.pubClient.publish(
          RedisKey.HEATMAP_PREDICTIVE_CHANNEL,
          JSON.stringify(payload),
        );

        // Save latest
        await this.redisService.client.set(
          'game:heatmap:predictive:latest',
          JSON.stringify(payload)
        );

        this.logger.debug(
          `Predictive Heatmap published: ${totalOnline} online, ${cells.length} future active cells. Took ${Date.now() - startTime}ms`,
        );
      } else {
        this.logger.debug(
          `Custom Predictive Heatmap generated for ${targetTimestampMs}: ${totalOnline} online, ${cells.length} future active cells. Took ${Date.now() - startTime}ms`,
        );
      }

      return payload;
    } catch (err) {
      if (err instanceof Error) {
        this.logger.error(`Predictive heatmap aggregation failed: ${err.message}`);
      }
      return null;
    }
  }

  private getActivityMultiplier(targetTimestampMs?: number): number {
    if (!targetTimestampMs) return 1.0;

    // Load keyframes from ConfigService or use default
    let keyframes = DEFAULT_WEEKDAY_PROFILE;
    const customKeyframesRaw = this.configService.get<string>('CAMPUS_ACTIVITY_KEYFRAMES');
    if (customKeyframesRaw) {
      try {
        keyframes = JSON.parse(customKeyframesRaw);
      } catch (err) {
        this.logger.warn(`Failed to parse CAMPUS_ACTIVITY_KEYFRAMES: ${err}`);
      }
    }

    const weekendFactor = this.configService.get<number>('CAMPUS_WEEKEND_FACTOR', 0.3);

    return calculateActivityMultiplier(targetTimestampMs, keyframes, weekendFactor);
  }

  private async fetchActiveEvents(targetTimestampMs?: number): Promise<ActiveEvent[]> {
    try {
      const timeMs = targetTimestampMs || Date.now();
      const host = this.configService.get<string>('CONTEXT_SERVICE_HOST', 'localhost');
      const port = this.configService.get<number>('CONTEXT_SERVICE_REST_PORT', 8080);
      const url = `http://${host}:${port}/api/events/active-at?time=${timeMs}`;
      const res = await fetch(url);
      if (!res.ok) {
        this.logger.warn(`Failed to fetch active events: ${res.statusText}`);
        return [];
      }
      const events: ActiveEvent[] = await res.json();
      return events;
    } catch (err) {
      this.logger.error(`Error fetching active events: ${err}`);
      return [];
    }
  }

  getLatestHeatmap(): PredictiveHeatmapPayload | null {
    return this.latestHeatmap;
  }
}
