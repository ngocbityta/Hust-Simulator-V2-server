import { Injectable, Logger, OnModuleInit, Inject } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { ISpatialService } from '../spatial/spatial.interface';
import { IIntentService } from '../intent/intent.interface';
import { RedisService } from '../redis/redis.service';
import { IntentPrediction } from '../intent/intent.interface';
import { RedisKey } from '../common/enums/redis-key.enum';
import { getCampusPhase, getDistance, getPolygonCellsWithGaussian, interpolatePolyline } from '../common/utils/geo.util';
import { CampusDataUtil } from '../common/utils/campus-data.util';
import { ContextApiService, Poi } from './services/context-api.service';
import { HeatmapMultiplierService } from './services/heatmap-multiplier.service';
import { distributeFlockingWeight } from './utils/flocking.util';

export interface PredictiveHeatmapCell {
  cellX: number;
  cellY: number;
  count: number;
  centerLat: number;
  centerLng: number;
  intents: Record<string, number>;
}

export interface PredictiveHeatmapPayload {
  timestamp: number;
  totalOnline: number;
  cells: PredictiveHeatmapCell[];
  globalReasons?: string[];
  poiReasons?: Record<string, string[]>;
  simulationApplied?: boolean;
  simulationReasons?: string[];
}

export interface SimulationParams {
  weatherOverride?: { temp: number; rain: number } | null;
  virtualEvents?: Array<{
    name: string;
    buildingId: string;
    estimatedParticipants: number;
    startTime: string;
    endTime: string;
  }>;
  eventModifications?: Array<{
    originalEventName: string;
    action: 'modify' | 'remove';
    newBuildingId?: string;
    newStartTime?: string;
    newEndTime?: string;
    newParticipants?: number;
  }>;
  closedBuildingIds?: string[];
  closedNodeIds?: string[];
  userCountMultiplier?: number;
}

@Injectable()
export class PredictiveHeatmapService implements OnModuleInit {
  private readonly logger = new Logger(PredictiveHeatmapService.name);
  private poisMap = new Map<string, Poi>();
  private userIntentCache = new Map<string, { prediction: IntentPrediction; timestamp: number; lat: number; lng: number }>();
  private latestHeatmap: PredictiveHeatmapPayload | null = null;
  private isGenerating = false;

  constructor(
    private configService: ConfigService,
    @Inject(ISpatialService) private spatialService: ISpatialService,
    @Inject(IIntentService) private intentService: IIntentService,
    private redisService: RedisService,
    private contextApiService: ContextApiService,
    private heatmapMultiplierService: HeatmapMultiplierService,
  ) {}

  async onModuleInit() {
    this.logger.log('PredictiveHeatmapService initialized');
    
    const host = this.configService.get<string>('CONTEXT_SERVICE_HOST', 'localhost');
    const port = this.configService.get<number>('CONTEXT_SERVICE_REST_PORT', 8080);
    const campusDataUtil = CampusDataUtil.getInstance(host, port);

    try {
      await campusDataUtil.initialize();
      this.logger.log(`Loaded campus data (nodes/ways) from context-service DB.`);
    } catch (err) {
      this.logger.error(`Failed to initialize campus data, will retry on next cycle: ${err}`);
    }

    const intervalMs = this.configService.get<number>('HEATMAP_PREDICTIVE_INTERVAL_MS', 15000);
    setInterval(() => this.aggregatePredictiveHeatmap(), intervalMs);
    this.logger.log(`Predictive heatmap interval set to ${intervalMs}ms`);
  }

  async aggregatePredictiveHeatmap(): Promise<void> {
    await this.generateHeatmap();
  }

  async generateHeatmap(targetTimestampMs?: number): Promise<PredictiveHeatmapPayload | null> {
    if (this.isGenerating) return null;
    this.isGenerating = true;

    if (this.poisMap.size === 0) {
      this.poisMap = await this.contextApiService.loadPois();
    }

    const startTime = Date.now();
    try {
      const cellSize = this.spatialService.getCellSize();
      const metersPerLat = this.spatialService.getMetersPerLat();
      const metersPerLng = this.spatialService.getMetersPerLng();
      const CACHE_TTL_MS = this.configService.get<number>('INTENT_CACHE_TTL_MS', 30000);

      // Prevent memory leak by pruning expired cache entries
      const now = Date.now();
      for (const [key, val] of this.userIntentCache.entries()) {
         if (now - val.timestamp > CACHE_TTL_MS) {
            this.userIntentCache.delete(key);
         }
      }

      let phaseInfo = getCampusPhase(targetTimestampMs);
      phaseInfo = this.heatmapMultiplierService.adjustTransitPhase(phaseInfo);

      const globalReasons = this.heatmapMultiplierService.getMultiplierReasons(targetTimestampMs);
      const poiReasons: Record<string, string[]> = {};

      const { transitRatio, nodeHotspots, wayDensityMultiplier } = phaseInfo;

      const userIds = await this.redisService.client.zrange(RedisKey.PLAYER_GEO_KEY, 0, -1);
      this.logger.debug(`[PredictiveHeatmapService] Fetched ${userIds.length} users from GEO_KEY`);
      if (userIds.length === 0) return null;

      const coords = await this.redisService.client.geopos(RedisKey.PLAYER_GEO_KEY, ...userIds);

      const cellMap = new Map<string, { cellX: number; cellY: number; count: number; intents: Map<string, number>; }>();
      const eventMultipliers = new Map<string, number>();
      const phantomDensities: Array<{ lat: number, lng: number, count: number, name: string }> = [];

      if (targetTimestampMs || true) { // Run in both live and predictive mode to fetch events
        const activeEvents = await this.contextApiService.fetchActiveEvents(targetTimestampMs || Date.now());
        const checkTimeMs = targetTimestampMs || Date.now();
        
        for (const ev of activeEvents) {
          if (ev.startTime && ev.endTime) {
            // Append +07:00 to correctly parse Vietnam time (GMT+7)
            const startStr = ev.startTime.replace(' ', 'T') + '+07:00';
            const endStr = ev.endTime.replace(' ', 'T') + '+07:00';
            const startMs = new Date(startStr).getTime();
            const endMs = new Date(endStr).getTime();
            
            // Allow events that start within 1 hour from checkTime, up to their endTime
            if (checkTimeMs < startMs - 3600000 || checkTimeMs > endMs) {
              continue;
            }
          }

          if (ev.buildingId && this.poisMap.has(ev.buildingId)) {
            const poi = this.poisMap.get(ev.buildingId);
            if (poi) {
              if (!poiReasons[poi.name]) poiReasons[poi.name] = [];
              poiReasons[poi.name].push(`Sự kiện "${ev.name}" dự kiến diễn ra`);

              const participants = ev.estimatedParticipants || 0;
              const multi = 1 + (participants / 20.0);
              eventMultipliers.set(ev.buildingId, multi);

              if (participants > 0) {
                phantomDensities.push({
                  lat: poi.lat,
                  lng: poi.lng,
                  count: participants * this.heatmapMultiplierService.calculateActivityMultiplier(targetTimestampMs) * 0.5,
                  name: `[Event|${poi.name}] ${ev.name}`
                });
              }
            }
          }
        }
      }

      let globalTransitWeight = 0;
      let totalOnline = 0;

      const host = this.configService.get<string>('CONTEXT_SERVICE_HOST', 'localhost');
      const port = this.configService.get<number>('CONTEXT_SERVICE_REST_PORT', 8080);
      const campusDataUtil = CampusDataUtil.getInstance(host, port);
      
      if (!campusDataUtil.isInitialized()) {
        try {
          await campusDataUtil.initialize();
        } catch (err) {}
      }

      const predictionTasks: (() => Promise<void>)[] = [];
      let nullCoords = 0;
      let nanCoords = 0;

      for (let i = 0; i < userIds.length; i++) {
        const userId = userIds[i];
        const coord = coords[i];
        if (!coord) {
            nullCoords++;
            continue;
        }

        const lng = parseFloat(coord[0] as unknown as string);
        const lat = parseFloat(coord[1] as unknown as string);
        if (isNaN(lat) || isNaN(lng)) {
            nanCoords++;
            continue;
        }
        
        totalOnline++;

        const cacheKey = `${userId}:${targetTimestampMs || 'live'}`;

        predictionTasks.push(async () => {
          let prediction: IntentPrediction | null = null;
          const cached = this.userIntentCache.get(cacheKey);
          
          let isValidCache = false;
          if (cached && (startTime - cached.timestamp < CACHE_TTL_MS)) {
             const distMoved = getDistance(lat, lng, cached.lat, cached.lng);
             if (distMoved < 15.0) { // If user moved less than 15 meters, reuse intent prediction
                 isValidCache = true;
             }
          }

          if (isValidCache && cached) {
            prediction = cached.prediction;
          } else {
            prediction = await this.intentService.predictIntent(userId, lat, lng, undefined, targetTimestampMs);
            if (prediction) {
              this.userIntentCache.set(cacheKey, { prediction, timestamp: Date.now(), lat, lng });
            }
          }

          if (!prediction) {
             return;
           }

          const activityMultiplier = this.heatmapMultiplierService.calculateActivityMultiplier(targetTimestampMs);
          let candidates = prediction.candidateDestinations?.slice(0, 3) || [];
          if (candidates.length === 0 && prediction.predictedDestinationId) {
            candidates = [{
              poiId: prediction.predictedDestinationId,
              poiName: prediction.predictedDestinationName || '',
              lat: prediction.targetLat || 0,
              lng: prediction.targetLng || 0,
              probability: prediction.confidence || 1,
            }];
          }
          
          let totalProb = 0;
          for (const cand of candidates) {
            if (cand.poiId && eventMultipliers.has(cand.poiId)) {
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
            const scaledProb = (cand.probability || 1) * activityMultiplier;
             const destWeight = scaledProb * 1.0;
             const buildingWeight = destWeight * (1 - transitRatio);
             const transitWeight = destWeight * transitRatio;

            if (transitWeight > 0) {
               globalTransitWeight += transitWeight;
            }

            if (buildingWeight > 0) {
               const buildingName = cand.poiId ? (this.poisMap.get(cand.poiId)?.name || 'Building') : 'Building';
               const intentLabel = `[Event|${buildingName}] ${prediction.intent}`;

               // Try to spread density across building polygon with Gaussian falloff
               let spreadAcrossPolygon = false;
               if (cand.poiId && campusDataUtil.isInitialized()) {
                 const bpoly = campusDataUtil.getData().buildingPolygons.get(cand.poiId);
                 if (bpoly && bpoly.polygon.length >= 3) {
                   const gaussCells = getPolygonCellsWithGaussian(
                     bpoly.polygon, bpoly.centroidLng, bpoly.centroidLat,
                     cellSize, metersPerLat, metersPerLng,
                   );
                   if (gaussCells.length > 0) {
                     spreadAcrossPolygon = true;
                     for (const gc of gaussCells) {
                       const gcKey = `${gc.cellX}:${gc.cellY}`;
                       if (!cellMap.has(gcKey)) {
                         cellMap.set(gcKey, { cellX: gc.cellX, cellY: gc.cellY, count: 0, intents: new Map<string, number>() });
                       }
                       const cd = cellMap.get(gcKey)!;
                       const w = buildingWeight * gc.weight;
                       cd.count += w;
                       cd.intents.set(intentLabel, (cd.intents.get(intentLabel) || 0) + w);
                     }
                   }
                 }
               }

               // Fallback: use single centroid cell if no polygon available
               if (!spreadAcrossPolygon) {
                 const targetCell = this.spatialService.getGridCell(cand.lat, cand.lng);
                 const cellKey = this.spatialService.getCellKey(targetCell);
                 if (!cellMap.has(cellKey)) {
                   cellMap.set(cellKey, { cellX: targetCell.x, cellY: targetCell.y, count: 0, intents: new Map<string, number>() });
                 }
                 const cellData = cellMap.get(cellKey)!;
                 cellData.count += buildingWeight;
                 cellData.intents.set(intentLabel, (cellData.intents.get(intentLabel) || 0) + buildingWeight);
               }
            }
          }
        });
      }

      const batchSize = 100;
      for (let i = 0; i < predictionTasks.length; i += batchSize) {
        const batch = predictionTasks.slice(i, i + batchSize);
        await Promise.allSettled(batch.map(task => task()));
      }

      if (globalTransitWeight > 0 && campusDataUtil.isInitialized()) {
         const campusData = campusDataUtil.getData();
         const hotspotWeight = globalTransitWeight * 0.3;
         // wayDensityMultiplier already adjusts transitRatio via adjustTransitPhase,
         // don't multiply it again here — it was making roads invisible
         const wayWeight = globalTransitWeight * 0.7;

         this.logger.log(`[Transit Debug] phase=${phaseInfo.phase}, transitRatio=${transitRatio}, globalTransitWeight=${globalTransitWeight.toFixed(2)}, wayWeight=${wayWeight.toFixed(2)}, wayDensityMultiplier=${wayDensityMultiplier}, waysCount=${campusData.ways.length}`);

         let activeHotspots: any[] = [];
         if (nodeHotspots.includes('GATE')) activeHotspots.push(...campusData.gates);
         if (nodeHotspots.includes('PARKING')) activeHotspots.push(...campusData.parkingAreas);
         if (nodeHotspots.includes('CANTEEN')) activeHotspots.push(...campusData.canteenAreas);

         if (activeHotspots.length > 0) {
           const weightPerHotspot = hotspotWeight / activeHotspots.length;
           for (const node of activeHotspots) {
              phantomDensities.push({
                 lat: node.latitude,
                 lng: node.longitude,
                 count: weightPerHotspot,
                 name: `[Transit] ${node.name}`
              });
           }
         }

         if (campusData.ways.length > 0 && wayWeight > 0) {
            let totalWayScore = 0;
            const waysWithScore = campusData.ways.map(way => {
               let mult = 1.0;
               if (way.wayType === 'ROAD') mult = 3.0;
               else if (way.wayType === 'ALLEY') mult = 0.5;
               
               const score = Math.max(10, way.distanceMeters) * mult;
               totalWayScore += score;
               return { way, score };
            });

            // Sort by score descending so main roads are processed first
            waysWithScore.sort((a, b) => b.score - a.score);

            let wayCellsAdded = 0;
            let waysSkipped = 0;
            const MAX_WAY_CELLS = 2500; // Cap total road cells to prevent payload bloat, increased to 2500 to cover campus
            for (const { way, score } of waysWithScore) {
               if (wayCellsAdded >= MAX_WAY_CELLS) break;
               const weightForThisWay = wayWeight * (score / totalWayScore);
               // Threshold lowered: with 440 roads, each gets ~wayWeight/440 share
               if (weightForThisWay < 0.001 || way.coordinates.length === 0) {
                   waysSkipped++;
                   continue;
               }
               // Interpolate points along the road
               const interpolatedCoords = interpolatePolyline(way.coordinates, cellSize * 0.5);
               
               // Phân bổ bầy đàn (Flocking)
               wayCellsAdded += distributeFlockingWeight(
                 interpolatedCoords,
                 weightForThisWay,
                 targetTimestampMs,
                 this.spatialService,
                 cellMap
               );
            }
            this.logger.log(`[Transit Debug] waysProcessed=${waysWithScore.length - waysSkipped}, waysSkipped=${waysSkipped}, wayCellsAdded=${wayCellsAdded}, totalWayScore=${totalWayScore.toFixed(1)}`);
         }
      } else {
         this.logger.log(`[Transit Debug] SKIPPED: globalTransitWeight=${globalTransitWeight.toFixed(2)}, campusDataInitialized=${campusDataUtil.isInitialized()}`);
      }

      for (const phantom of phantomDensities) {
        const targetCell = this.spatialService.getGridCell(phantom.lat, phantom.lng);
        const cellKey = this.spatialService.getCellKey(targetCell);

        if (!cellMap.has(cellKey)) {
          cellMap.set(cellKey, { cellX: targetCell.x, cellY: targetCell.y, count: 0, intents: new Map<string, number>() });
        }

        const cellData = cellMap.get(cellKey)!;
        cellData.count += phantom.count;
        cellData.intents.set(phantom.name, (cellData.intents.get(phantom.name) || 0) + phantom.count);
      }

      const cells: PredictiveHeatmapCell[] = [];
      for (const cellData of cellMap.values()) {
        const centerLng = ((cellData.cellX + 0.5) * cellSize) / metersPerLng;
        const centerLat = ((cellData.cellY + 0.5) * cellSize) / metersPerLat;

        const intents: Record<string, number> = {};
        for (const [intentName, count] of cellData.intents) {
          intents[intentName] = count;
        }

        cells.push({ cellX: cellData.cellX, cellY: cellData.cellY, count: cellData.count, centerLat, centerLng, intents });
      }

      const payload: PredictiveHeatmapPayload = {
        timestamp: targetTimestampMs || Date.now(),
        totalOnline: Math.round(totalOnline),
        cells,
        globalReasons,
        poiReasons,
      };

      if (!targetTimestampMs) {
        this.latestHeatmap = payload;
        await this.redisService.pubClient.publish(RedisKey.HEATMAP_PREDICTIVE_CHANNEL, JSON.stringify(payload));
        await this.redisService.client.set('game:heatmap:predictive:latest', JSON.stringify(payload));
        this.logger.debug(`Predictive Heatmap published: ${totalOnline} online. Took ${Date.now() - startTime}ms`);
      } else {
        this.logger.debug(`Forecast Heatmap generated for ${new Date(targetTimestampMs).toISOString()}: ${totalOnline} online. Took ${Date.now() - startTime}ms`);
      }

      return payload;
    } catch (err) {
      this.logger.error(`Error generating predictive heatmap: ${err}`);
      return null;
    } finally {
      this.isGenerating = false;
    }
  }

  getLatestHeatmap(): PredictiveHeatmapPayload | null {
    return this.latestHeatmap;
  }

  async generateSimulatedHeatmap(
    targetTimestampMs: number,
    params: SimulationParams,
  ): Promise<PredictiveHeatmapPayload | null> {
    // Re-use the existing isGenerating guard with a separate flag for simulation
    if (this.poisMap.size === 0) {
      this.poisMap = await this.contextApiService.loadPois();
    }

    const startTime = Date.now();
    const simulationReasons: string[] = [];

    try {
      const cellSize = this.spatialService.getCellSize();
      const metersPerLat = this.spatialService.getMetersPerLat();
      const metersPerLng = this.spatialService.getMetersPerLng();

      // ── 1. Determine Phase ──
      let phaseInfo = getCampusPhase(targetTimestampMs);
      phaseInfo = this.heatmapMultiplierService.adjustTransitPhase(phaseInfo);

      const globalReasons = this.heatmapMultiplierService.getMultiplierReasons(targetTimestampMs);
      const poiReasons: Record<string, string[]> = {};

      const { transitRatio, nodeHotspots, wayDensityMultiplier } = phaseInfo;

      const userIds = await this.redisService.client.zrange(RedisKey.PLAYER_GEO_KEY, 0, -1);
      if (userIds.length === 0) {
        return {
          timestamp: targetTimestampMs,
          totalOnline: 0,
          cells: [],
          globalReasons,
          poiReasons,
          simulationApplied: true,
          simulationReasons: [...simulationReasons, 'Không có user online'],
        };
      }

      const coords = await this.redisService.client.geopos(RedisKey.PLAYER_GEO_KEY, ...userIds);

      const cellMap = new Map<string, { cellX: number; cellY: number; count: number; intents: Map<string, number> }>();
      const eventMultipliers = new Map<string, number>();
      const phantomDensities: Array<{ lat: number; lng: number; count: number; name: string }> = [];

      // ── 2. Events: fetch real → apply modifications → inject virtual ──
      let activeEvents = await this.contextApiService.fetchActiveEvents(targetTimestampMs);
      const checkTimeMs = targetTimestampMs;

      // Apply event modifications
      if (params.eventModifications && params.eventModifications.length > 0) {
        for (const mod of params.eventModifications) {
          if (mod.action === 'remove') {
            activeEvents = activeEvents.filter(e => e.name !== mod.originalEventName);
            simulationReasons.push(`Xóa sự kiện: "${mod.originalEventName}"`);
          } else if (mod.action === 'modify') {
            const ev = activeEvents.find(e => e.name === mod.originalEventName);
            if (ev) {
              if (mod.newBuildingId) ev.buildingId = mod.newBuildingId;
              if (mod.newStartTime) ev.startTime = mod.newStartTime;
              if (mod.newEndTime) ev.endTime = mod.newEndTime;
              if (mod.newParticipants !== undefined) ev.estimatedParticipants = mod.newParticipants;
              simulationReasons.push(`Sửa sự kiện: "${mod.originalEventName}"`);
            }
          }
        }
      }

      // Inject virtual events
      if (params.virtualEvents && params.virtualEvents.length > 0) {
        for (const ve of params.virtualEvents) {
          activeEvents.push({
            buildingId: ve.buildingId,
            name: ve.name,
            estimatedParticipants: ve.estimatedParticipants,
            startTime: ve.startTime,
            endTime: ve.endTime,
          });
          simulationReasons.push(`Thêm sự kiện giả lập: "${ve.name}" (${ve.estimatedParticipants} người)`);
        }
      }

      // ── Closed buildings ──
      const closedBuildingSet = new Set(params.closedBuildingIds || []);
      if (closedBuildingSet.size > 0) {
        const closedNames = [...closedBuildingSet]
          .map(id => this.poisMap.get(id)?.name || id)
          .join(', ');
        simulationReasons.push(`Đóng cửa tòa nhà: ${closedNames}`);
      }

      // ── Closed nodes ──
      const closedNodeSet = new Set(params.closedNodeIds || []);
      if (closedNodeSet.size > 0) {
        simulationReasons.push(`Đóng cửa ${closedNodeSet.size} cổng/nhà xe`);
      }

      // ── Process events into multipliers and phantoms (same logic as generateHeatmap) ──
      for (const ev of activeEvents) {
        if (ev.startTime && ev.endTime) {
          const startStr = ev.startTime.replace(' ', 'T') + (ev.startTime.includes('+') ? '' : '+07:00');
          const endStr = ev.endTime.replace(' ', 'T') + (ev.endTime.includes('+') ? '' : '+07:00');
          const startMs = new Date(startStr).getTime();
          const endMs = new Date(endStr).getTime();
          if (checkTimeMs < startMs - 3600000 || checkTimeMs > endMs) {
            continue;
          }
        }

        if (ev.buildingId && this.poisMap.has(ev.buildingId)) {
          // Skip events at closed buildings
          if (closedBuildingSet.has(ev.buildingId)) continue;

          const poi = this.poisMap.get(ev.buildingId);
          if (poi) {
            if (!poiReasons[poi.name]) poiReasons[poi.name] = [];
            poiReasons[poi.name].push(`Sự kiện "${ev.name}" dự kiến diễn ra`);

            const participants = ev.estimatedParticipants || 0;
            const multi = 1 + (participants / 20.0);
            eventMultipliers.set(ev.buildingId, multi);

            if (participants > 0) {
              phantomDensities.push({
                lat: poi.lat,
                lng: poi.lng,
                count: participants * this.heatmapMultiplierService.calculateActivityMultiplier(targetTimestampMs) * 0.5,
                name: `[Event|${poi.name}] ${ev.name}`,
              });
            }
          }
        }
      }

      // ── User processing (same as generateHeatmap) ──
      let globalTransitWeight = 0;
      let totalOnline = 0;
      const CACHE_TTL_MS = this.configService.get<number>('INTENT_CACHE_TTL_MS', 30000);

      const host = this.configService.get<string>('CONTEXT_SERVICE_HOST', 'localhost');
      const port = this.configService.get<number>('CONTEXT_SERVICE_REST_PORT', 8080);
      const campusDataUtil = CampusDataUtil.getInstance(host, port);
      if (!campusDataUtil.isInitialized()) {
        try { await campusDataUtil.initialize(); } catch {}
      }

      const predictionTasks: (() => Promise<void>)[] = [];

      for (let i = 0; i < userIds.length; i++) {
        const userId = userIds[i];
        const coord = coords[i];
        if (!coord) continue;

        const lng = parseFloat(coord[0] as unknown as string);
        const lat = parseFloat(coord[1] as unknown as string);
        if (isNaN(lat) || isNaN(lng)) continue;

        totalOnline++;

        const cacheKey = `sim:${userId}:${targetTimestampMs}`;

        predictionTasks.push(async () => {
          let prediction: IntentPrediction | null = null;
          const cached = this.userIntentCache.get(cacheKey);

          let isValidCache = false;
          if (cached && (startTime - cached.timestamp < CACHE_TTL_MS)) {
            const distMoved = getDistance(lat, lng, cached.lat, cached.lng);
            if (distMoved < 15.0) isValidCache = true;
          }

          if (isValidCache && cached) {
            prediction = cached.prediction;
          } else {
            prediction = await this.intentService.predictIntent(userId, lat, lng, undefined, targetTimestampMs);
            if (prediction) {
              this.userIntentCache.set(cacheKey, { prediction, timestamp: Date.now(), lat, lng });
            }
          }

          if (!prediction) {
            return;
          }

          const activityMultiplier = this.heatmapMultiplierService.calculateActivityMultiplier(targetTimestampMs);
          let candidates = prediction.candidateDestinations?.slice(0, 3) || [];
          if (candidates.length === 0 && prediction.predictedDestinationId) {
            candidates = [{
              poiId: prediction.predictedDestinationId,
              poiName: prediction.predictedDestinationName || '',
              lat: prediction.targetLat || 0,
              lng: prediction.targetLng || 0,
              probability: prediction.confidence || 1,
            }];
          }

          // Filter out closed buildings from candidates
          if (closedBuildingSet.size > 0) {
            candidates = candidates.filter(c => !c.poiId || !closedBuildingSet.has(c.poiId));
          }

          let totalProb = 0;
          for (const cand of candidates) {
            if (cand.poiId && eventMultipliers.has(cand.poiId)) {
              cand.probability *= eventMultipliers.get(cand.poiId)!;
            }
            totalProb += cand.probability;
          }
          if (totalProb > 0) {
            for (const cand of candidates) {
              cand.probability /= totalProb;
            }
          }

          if (candidates.length === 0) {
            // All destinations closed — ignore user
            return;
          }

          for (const cand of candidates) {
            const scaledProb = (cand.probability || 1) * activityMultiplier;
            const destWeight = scaledProb * 1.0;
            const buildingWeight = destWeight * (1 - transitRatio);
            const transitWeight = destWeight * transitRatio;

            if (transitWeight > 0) globalTransitWeight += transitWeight;

            if (buildingWeight > 0) {
              const buildingName = cand.poiId ? (this.poisMap.get(cand.poiId)?.name || 'Building') : 'Building';
              const intentLabel = `[Event|${buildingName}] ${prediction.intent}`;

              let spreadAcrossPolygon = false;
              if (cand.poiId && campusDataUtil.isInitialized()) {
                const bpoly = campusDataUtil.getData().buildingPolygons.get(cand.poiId);
                if (bpoly && bpoly.polygon.length >= 3) {
                  const gaussCells = getPolygonCellsWithGaussian(
                    bpoly.polygon, bpoly.centroidLng, bpoly.centroidLat,
                    cellSize, metersPerLat, metersPerLng,
                  );
                  if (gaussCells.length > 0) {
                    spreadAcrossPolygon = true;
                    for (const gc of gaussCells) {
                      const gcKey = `${gc.cellX}:${gc.cellY}`;
                      if (!cellMap.has(gcKey)) {
                        cellMap.set(gcKey, { cellX: gc.cellX, cellY: gc.cellY, count: 0, intents: new Map<string, number>() });
                      }
                      const cd = cellMap.get(gcKey)!;
                      const w = buildingWeight * gc.weight;
                      cd.count += w;
                      cd.intents.set(intentLabel, (cd.intents.get(intentLabel) || 0) + w);
                    }
                  }
                }
              }

              if (!spreadAcrossPolygon) {
                const targetCell = this.spatialService.getGridCell(cand.lat, cand.lng);
                const tCellKey = this.spatialService.getCellKey(targetCell);
                if (!cellMap.has(tCellKey)) {
                  cellMap.set(tCellKey, { cellX: targetCell.x, cellY: targetCell.y, count: 0, intents: new Map<string, number>() });
                }
                const cellData = cellMap.get(tCellKey)!;
                cellData.count += buildingWeight;
                cellData.intents.set(intentLabel, (cellData.intents.get(intentLabel) || 0) + buildingWeight);
              }
            }
          }
        });
      }

      // Process in batches
      const batchSize = 100;
      for (let i = 0; i < predictionTasks.length; i += batchSize) {
        const batch = predictionTasks.slice(i, i + batchSize);
        await Promise.allSettled(batch.map(task => task()));
      }

      // ── Transit distribution (filter out closed nodes) ──
      if (globalTransitWeight > 0 && campusDataUtil.isInitialized()) {
        const campusData = campusDataUtil.getData();
        const hotspotWeight = globalTransitWeight * 0.3;
        const wayWeight = globalTransitWeight * 0.7;

        let activeHotspots: any[] = [];
        if (nodeHotspots.includes('GATE')) activeHotspots.push(...campusData.gates);
        if (nodeHotspots.includes('PARKING')) activeHotspots.push(...campusData.parkingAreas);
        if (nodeHotspots.includes('CANTEEN')) activeHotspots.push(...campusData.canteenAreas);

        // Filter closed nodes
        if (closedNodeSet.size > 0) {
          activeHotspots = activeHotspots.filter((n: any) => !closedNodeSet.has(n.id));
        }

        if (activeHotspots.length > 0) {
          const weightPerHotspot = hotspotWeight / activeHotspots.length;
          for (const node of activeHotspots) {
            phantomDensities.push({
              lat: node.latitude,
              lng: node.longitude,
              count: weightPerHotspot,
              name: `[Transit] ${node.name}`,
            });
          }
        }

        if (campusData.ways.length > 0 && wayWeight > 0) {
          let totalWayScore = 0;
          const waysWithScore = campusData.ways.map(way => {
            let mult = 1.0;
            if (way.wayType === 'ROAD') mult = 3.0;
            else if (way.wayType === 'ALLEY') mult = 0.5;
            const score = Math.max(10, way.distanceMeters) * mult;
            totalWayScore += score;
            return { way, score };
          });
          waysWithScore.sort((a, b) => b.score - a.score);

          let wayCellsAdded = 0;
          const MAX_WAY_CELLS = 2500;
          for (const { way, score } of waysWithScore) {
            if (wayCellsAdded >= MAX_WAY_CELLS) break;
            const weightForThisWay = wayWeight * (score / totalWayScore);
            if (weightForThisWay < 0.001 || way.coordinates.length === 0) continue;
            const interpolatedCoords = interpolatePolyline(way.coordinates, cellSize * 0.5);
            wayCellsAdded += distributeFlockingWeight(
              interpolatedCoords, weightForThisWay, targetTimestampMs,
              this.spatialService, cellMap,
            );
          }
        }
      }

      // ── Phantom densities ──
      for (const phantom of phantomDensities) {
        const targetCell = this.spatialService.getGridCell(phantom.lat, phantom.lng);
        const cellKey = this.spatialService.getCellKey(targetCell);
        if (!cellMap.has(cellKey)) {
          cellMap.set(cellKey, { cellX: targetCell.x, cellY: targetCell.y, count: 0, intents: new Map<string, number>() });
        }
        const cellData = cellMap.get(cellKey)!;
        cellData.count += phantom.count;
        cellData.intents.set(phantom.name, (cellData.intents.get(phantom.name) || 0) + phantom.count);
      }

      // ── Build cells array ──
      const cells: PredictiveHeatmapCell[] = [];
      for (const cellData of cellMap.values()) {
        const centerLng = ((cellData.cellX + 0.5) * cellSize) / metersPerLng;
        const centerLat = ((cellData.cellY + 0.5) * cellSize) / metersPerLat;
        const intents: Record<string, number> = {};
        for (const [intentName, count] of cellData.intents) {
          intents[intentName] = count;
        }
        cells.push({ cellX: cellData.cellX, cellY: cellData.cellY, count: cellData.count, centerLat, centerLng, intents });
      }

      // ── 3. Apply user count multiplier ──
      const multiplier = params.userCountMultiplier ?? 1.0;
      if (multiplier !== 1.0) {
        for (const cell of cells) {
          cell.count *= multiplier;
          for (const key of Object.keys(cell.intents)) {
            cell.intents[key] *= multiplier;
          }
        }
        simulationReasons.push(`Nhân số lượng người x${multiplier}`);
      }

      const payload: PredictiveHeatmapPayload = {
        timestamp: targetTimestampMs,
        totalOnline: Math.round(totalOnline * multiplier),
        cells,
        globalReasons,
        poiReasons,
        simulationApplied: true,
        simulationReasons,
      };

      this.logger.log(`Simulation heatmap generated for ${new Date(targetTimestampMs).toISOString()}: ${totalOnline} online (x${multiplier}). Took ${Date.now() - startTime}ms. Overrides: ${simulationReasons.join('; ')}`);
      return payload;
    } catch (err) {
      this.logger.error(`Error generating simulated heatmap: ${err}`);
      return null;
    }
  }
}
