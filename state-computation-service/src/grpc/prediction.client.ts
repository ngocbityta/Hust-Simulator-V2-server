import { Inject, Injectable, OnModuleInit, Logger } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { Observable, firstValueFrom } from 'rxjs';

// Matching prediction.proto definition
export interface TrajectoryPoint {
  latitude: number;
  longitude: number;
  timestamp: number;
}

export interface PredictNextLocationRequest {
  userId: string;
  trajectory: TrajectoryPoint[];
  currentHeading: number;
}

export interface PredictNextLocationResponse {
  predictedPoiId: string;
  predictedPoiName: string;
  confidence: number;
  intentType: string;
  targetLat: number;
  targetLng: number;
}

interface PredictionServiceGrpc {
  predictNextLocation(
    request: PredictNextLocationRequest,
  ): Observable<PredictNextLocationResponse>;
}

@Injectable()
export class GrpcPredictionClient implements OnModuleInit {
  private readonly logger = new Logger(GrpcPredictionClient.name);
  private predictionService!: PredictionServiceGrpc;

  constructor(
    @Inject('PREDICTION_SERVICE') private readonly client: ClientGrpc,
  ) {}

  onModuleInit() {
    this.predictionService = this.client.getService<PredictionServiceGrpc>(
      'PredictionService',
    );
    this.logger.log('gRPC Prediction Service client initialized');
  }

  async predictNextLocation(
    request: PredictNextLocationRequest,
  ): Promise<PredictNextLocationResponse> {
    return firstValueFrom(this.predictionService.predictNextLocation(request));
  }
}
