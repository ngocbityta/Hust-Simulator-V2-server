import { Injectable, Logger } from '@nestjs/common';
import { IIntentService, IntentPrediction } from './intent.interface';
import { HeuristicPredictor } from './predictors/heuristic.predictor';
import { AIPredictor } from './predictors/ai.predictor';
import { TrajectoryService } from './trajectory.service';
import { IntentType } from '../common/enums/intent.enum';

@Injectable()
export class IntentService implements IIntentService {
  private readonly logger = new Logger(IntentService.name);

  constructor(
    private readonly heuristicPredictor: HeuristicPredictor,
    private readonly aiPredictor: AIPredictor,
    private readonly trajectoryService: TrajectoryService,
  ) {}

  async predictIntent(
    userId: string,
    currentLat: number,
    currentLng: number,
    clientHeading?: number,
  ): Promise<IntentPrediction | null> {
    try {
      // 1. Log the new point into trajectory
      await this.trajectoryService.addPoint(userId, currentLat, currentLng, Date.now());

      // 2. Fetch recent trajectory
      const trajectory = await this.trajectoryService.getTrajectory(userId);

      // 3. Hybrid Strategy
      let aiPrediction: IntentPrediction | null = null;
      
      // Try AI Predictor if we have enough data (e.g., 5 points)
      const context = {
        userId,
        currentLat,
        currentLng,
        clientHeading,
        trajectory,
      };

      if (trajectory && trajectory.length >= 5) {
        aiPrediction = await this.aiPredictor.predict(context);
      }

      // If AI prediction is highly confident, use it
      if (aiPrediction && aiPrediction.confidence >= 0.8) {
        this.logger.debug(`[Hybrid] Using AI Prediction for ${userId}: ${aiPrediction.predictedDestinationName}`);
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
