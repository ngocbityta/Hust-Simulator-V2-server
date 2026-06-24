import { Injectable, Logger } from '@nestjs/common';
import { IIntentService, IntentPrediction } from './intent.interface';
import { HeuristicPredictor } from './predictors/heuristic.predictor';
import { AIPredictor } from './predictors/ai.predictor';
import { IntentType } from '../common/enums/intent.enum';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class IntentService implements IIntentService {
  private readonly logger = new Logger(IntentService.name);

  constructor(
    private readonly heuristicPredictor: HeuristicPredictor,
    private readonly aiPredictor: AIPredictor,
    private readonly configService: ConfigService,
  ) { }

  async predictIntent(
    userId: string,
    currentLat: number,
    currentLng: number,
    clientHeading?: number,
    targetTimestampMs?: number,
  ): Promise<IntentPrediction | null> {
    try {
      // 1. Hybrid Strategy
      let aiPrediction: IntentPrediction | null = null;

      const context = {
        userId,
        currentLat,
        currentLng,
        clientHeading,
        targetTimestampMs,
      };

      const forceHeuristicVal = this.configService.get('USE_HEURISTIC_ONLY');
      const forceHeuristic = forceHeuristicVal === true || forceHeuristicVal === 'true';

      if (!forceHeuristic) {
        this.logger.debug(`[Hybrid] Calling AI Predictor for ${userId}`);
        aiPrediction = await this.aiPredictor.predict(context);
        this.logger.debug(`[Hybrid] AI Predictor returned: ${JSON.stringify(aiPrediction)}`);
      } else {
        this.logger.debug(`[Hybrid] Skipped AI Predictor. forceHeuristic=${forceHeuristic}`);
      }

      if (aiPrediction) {
        this.logger.debug(`[Hybrid] AI Prediction generated: ${aiPrediction.predictedDestinationName}, confidence: ${aiPrediction.confidence}`);
      }

      // If AI prediction is highly confident, use it
      const thresholdVal = this.configService.get('AI_CONFIDENCE_THRESHOLD');
      const threshold = thresholdVal !== undefined ? parseFloat(thresholdVal) : 0.08;
      if (aiPrediction && aiPrediction.confidence >= threshold) {
        this.logger.debug(`[Hybrid] Using AI Prediction for ${userId}: ${aiPrediction.predictedDestinationName} (confidence: ${aiPrediction.confidence.toFixed(3)})`);
        return aiPrediction;
      }

      // 4. Fallback to Heuristic Predictor
      this.logger.debug(`[Hybrid] Using Heuristic Fallback for ${userId}`);
      const heuristicPrediction = await this.heuristicPredictor.predict(context);

      // If AI prediction exists but has low confidence, we can compare or merge
      // For now, if heuristic is also low confidence, we can prefer AI or vice versa.
      if (heuristicPrediction) {
        return heuristicPrediction;
      }

      // Default fallback
      return {
        userId,
        intent: IntentType.WANDERING,
        confidence: 0.5,
        timestamp: Date.now(),
      };

    } catch (error: unknown) {
      const message = error instanceof Error ? error.message : String(error);
      this.logger.error(`Failed to predict intent for user ${userId}: ${message}`);
      return null;
    }
  }
}
