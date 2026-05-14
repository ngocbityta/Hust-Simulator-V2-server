import { Injectable, Logger } from '@nestjs/common';
import { GrpcPredictionClient, TrajectoryPoint } from '../../grpc/prediction.client';
import { IPredictor, IntentPrediction, PredictContext } from '../intent.interface';
import { IntentType } from '../../common/enums/intent.enum';

@Injectable()
export class AIPredictor implements IPredictor {
  private readonly logger = new Logger(AIPredictor.name);

  constructor(private readonly grpcPredictionClient: GrpcPredictionClient) {}

  async predict(context: PredictContext): Promise<IntentPrediction | null> {
    const { userId, trajectory, clientHeading } = context;

    if (!trajectory || trajectory.length < 5) {
      this.logger.debug(`Insufficient trajectory data for user ${userId}. Need at least 5 points.`);
      return null;
    }

    try {
      const response = await this.grpcPredictionClient.predictNextLocation({
        userId,
        trajectory,
        currentHeading: clientHeading || 0,
      });

      return {
        userId,
        predictedDestinationId: response.predictedPoiId,
        predictedDestinationName: response.predictedPoiName,
        targetLat: response.targetLat,
        targetLng: response.targetLng,
        intent: response.intentType as IntentType,
        confidence: response.confidence,
        timestamp: Date.now(),
      };
    } catch (error) {
      this.logger.error(`AI Prediction failed for user ${userId}: ${error}`);
      return null;
    }
  }
}
