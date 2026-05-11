import { IntentType } from '../common/enums/intent.enum';

export interface IntentPrediction {
  userId: string;
  predictedDestinationId?: string;
  predictedDestinationName?: string;
  intent: IntentType;
  confidence: number;
  timestamp: number;
}

export interface IIntentService {
  predictIntent(
    userId: string,
    currentLat: number,
    currentLng: number,
    clientHeading?: number,
  ): Promise<IntentPrediction | null>;
}

export const IIntentService = Symbol('IIntentService');
