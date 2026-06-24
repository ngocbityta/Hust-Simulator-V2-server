import { Injectable, Logger } from '@nestjs/common';
import { GrpcPredictionClient } from '../../grpc/prediction.client';
import { IPredictor, IntentPrediction, PredictContext } from '../intent.interface';
import { IntentType } from '../../common/enums/intent.enum';

@Injectable()
export class AIPredictor implements IPredictor {
  private readonly logger = new Logger(AIPredictor.name);

  constructor(private readonly grpcPredictionClient: GrpcPredictionClient) {}

  async predict(context: PredictContext): Promise<IntentPrediction | null> {
    const { userId, clientHeading } = context;

    try {
      const response = await this.grpcPredictionClient.predictNextLocation({
        userId,
        currentHeading: clientHeading || 0,
        targetTimestampMs: context.targetTimestampMs,
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
        candidateDestinations: response.candidateDestinations,
      };
    } catch (error) {
      this.logger.error(`AI Prediction failed for user ${userId}: ${error}`);
      return null;
    }
  }
}
