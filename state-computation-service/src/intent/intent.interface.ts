import { IntentType } from '../common/enums/intent.enum';
import { PoiPrediction } from '../grpc/prediction.client';

export interface PredictContext {
  userId: string;
  currentLat: number;
  currentLng: number;
  clientHeading?: number;
  targetTimestampMs?: number;
}

export interface IPredictor {
  predict(context: PredictContext): Promise<IntentPrediction | null>;
}

export interface IntentPrediction {
  userId: string;
  predictedDestinationId?: string;
  predictedDestinationName?: string;
  targetLat?: number;
  targetLng?: number;
  intent: IntentType;
  confidence: number;
  timestamp: number;
  candidateDestinations?: PoiPrediction[];
}

export interface IIntentService {
  predictIntent(
    userId: string,
    currentLat: number,
    currentLng: number,
    clientHeading?: number,
    targetTimestampMs?: number,
  ): Promise<IntentPrediction | null>;
}

export const IIntentService = Symbol('IIntentService');
