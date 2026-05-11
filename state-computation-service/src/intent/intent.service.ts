import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as turf from '@turf/turf';
import { RedisService } from '../redis/redis.service';
import {
  GrpcContextClient,
  ActiveEventsResponse,
} from '../grpc/context.client';
import { RedisKey } from '../common/enums/redis-key.enum';

import { IIntentService, IntentPrediction } from './intent.interface';
import { IntentPayloadKey, IntentType } from '../common/enums/intent.enum';

@Injectable()
export class IntentService implements IIntentService {
  private readonly logger = new Logger(IntentService.name);

  // Configuration parameters
  private readonly confidenceThreshold: number;
  private readonly maxDistanceMeters: number;
  private readonly headingWeight: number;
  private readonly stationaryThresholdMeters: number;

  // Cache for active events
  private eventCache = new Map<
    string,
    { events: ActiveEventsResponse; timestamp: number }
  >();
  private readonly cacheTtlMs: number;

  constructor(
    private readonly redisService: RedisService,
    private readonly grpcContextClient: GrpcContextClient,
    private readonly configService: ConfigService,
  ) {
    this.confidenceThreshold = this.configService.get<number>(
      'INTENT_CONFIDENCE_THRESHOLD',
      0.6,
    );
    this.cacheTtlMs = this.configService.get<number>(
      'INTENT_CACHE_TTL_MS',
      30000,
    );
    this.maxDistanceMeters = this.configService.get<number>(
      'INTENT_MAX_DISTANCE',
      500,
    );
    this.headingWeight = this.configService.get<number>(
      'INTENT_HEADING_WEIGHT',
      0.7,
    );
    this.stationaryThresholdMeters = this.configService.get<number>(
      'INTENT_STATIONARY_THRESHOLD',
      0.5,
    );
  }

  async predictIntent(
    userId: string,
    currentLat: number,
    currentLng: number,
    clientHeading?: number,
  ): Promise<IntentPrediction | null> {
    try {
      // 1. Get previous state from Redis to calculate movement vector
      const playerStateKey = `${RedisKey.PLAYER_STATE_PREFIX}${userId}`;
      const state = await this.redisService.client.hgetall(playerStateKey);

      if (!state || Object.keys(state).length === 0) return null;

      const prevLat = parseFloat(state.latitude || '0');
      const prevLng = parseFloat(state.longitude || '0');
      const isOnline = state.isOnline === 'true';

      if (!isOnline || (prevLat === 0 && prevLng === 0)) {
        return {
          userId,
          intent: IntentType.STATIONARY,
          confidence: 1.0,
          timestamp: Date.now(),
        };
      }

      // 2. Check if moved significantly
      const distanceMoved = turf.distance(
        turf.point([prevLng, prevLat]),
        turf.point([currentLng, currentLat]),
        { units: 'meters' },
      );

      if (distanceMoved < this.stationaryThresholdMeters) {
        return {
          userId,
          intent: IntentType.STATIONARY,
          confidence: 0.9,
          timestamp: Date.now(),
        };
      }

      // 3. Calculate current heading
      // Use client-provided heading if available and valid, otherwise calculate from movement
      let currentHeading = clientHeading;
      if (currentHeading === undefined || isNaN(currentHeading)) {
        currentHeading = turf.bearing(
          turf.point([prevLng, prevLat]),
          turf.point([currentLng, currentLat]),
        );
      }

      // 4. Fetch upcoming events from Context Service as potential destinations (with cache)
      const now = Date.now();
      let userCache = this.eventCache.get(userId);

      if (!userCache || now - userCache.timestamp > this.cacheTtlMs) {
        const events = await this.grpcContextClient.getActiveEvents(userId);
        userCache = { events, timestamp: now };
        this.eventCache.set(userId, userCache);

        // Optional: Simple cleanup of expired caches to prevent memory leak
        if (this.eventCache.size > 1000) {
          for (const [key, cache] of this.eventCache.entries()) {
            if (now - cache.timestamp > this.cacheTtlMs) {
              this.eventCache.delete(key);
            }
          }
        }
      }
      const activeEvents = userCache.events;

      const potentialDestinations = (activeEvents?.events || [])
        .map((event) => ({
          id: event.eventId,
          name: event.title,
          lat: parseFloat(event.payload?.[IntentPayloadKey.LATITUDE] || '0'),
          lng: parseFloat(event.payload?.[IntentPayloadKey.LONGITUDE] || '0'),
        }))
        .filter((d) => d.lat !== 0 && d.lng !== 0);

      if (potentialDestinations.length === 0) {
        this.logger.debug(`No valid destinations found for user ${userId}`);
        return {
          userId,
          intent: IntentType.WANDERING,
          confidence: 0.5,
          timestamp: Date.now(),
        };
      }

      // 5. Score potential destinations
      const predictions = potentialDestinations.map((dest) => {
        const bearingToDest = turf.bearing(
          turf.point([currentLng, currentLat]),
          turf.point([dest.lng, dest.lat]),
        );

        const distToDest = turf.distance(
          turf.point([currentLng, currentLat]),
          turf.point([dest.lng, dest.lat]),
          { units: 'meters' },
        );

        // Angle deviation (smaller is better)
        let angleDiff = Math.abs(currentHeading - bearingToDest);
        if (angleDiff > 180) angleDiff = 360 - angleDiff;

        /**
         * Scoring logic:
         * - Angle score: 1.0 if angle < 10 deg, 0.0 if angle > 90 deg
         * - Distance score: closer is better if moving towards it
         */
        const angleScore = Math.max(0, 1 - angleDiff / 90);
        const distScore = Math.max(0, 1 - distToDest / this.maxDistanceMeters);

        // Combined score based on configurable weight
        const totalScore =
          angleScore * this.headingWeight +
          distScore * (1 - this.headingWeight);

        return { score: totalScore, dest };
      });

      const bestMatch = predictions.sort((a, b) => b.score - a.score)[0];

      if (bestMatch && bestMatch.score > this.confidenceThreshold) {
        this.logger.debug(
          `User ${userId} predicted ${IntentType.GOING_TO_EVENT}: ${bestMatch.dest.name} (Score: ${bestMatch.score.toFixed(2)})`,
        );
        return {
          userId,
          predictedDestinationId: bestMatch.dest.id,
          predictedDestinationName: bestMatch.dest.name,
          intent: IntentType.GOING_TO_EVENT,
          confidence: bestMatch.score,
          timestamp: Date.now(),
        };
      }

      return {
        userId,
        intent: IntentType.WANDERING,
        confidence: 0.6,
        timestamp: Date.now(),
      };
    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : String(error);
      this.logger.error(
        `Failed to predict intent for user ${userId}: ${message}`,
      );
      return null;
    }
  }
}
