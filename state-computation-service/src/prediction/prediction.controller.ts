import { Controller, Post, Body } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiProperty } from '@nestjs/swagger';
import { GrpcPredictionClient, PredictNextLocationRequest } from '../grpc/prediction.client';

export class TrajectoryPointDto {
  @ApiProperty({ description: 'Latitude', example: 21.003 })
  latitude: number;

  @ApiProperty({ description: 'Longitude', example: 105.84 })
  longitude: number;

  @ApiProperty({ description: 'Timestamp in milliseconds', example: 1684400000000 })
  timestamp: number;
}

export class PredictNextLocationDto {
  @ApiProperty({ description: 'User ID UUID string', example: '123e4567-e89b-12d3-a456-426614174000' })
  userId: string;

  @ApiProperty({ description: 'List of recent trajectory points', type: [TrajectoryPointDto] })
  trajectory: TrajectoryPointDto[];

  @ApiProperty({ description: 'Current heading in degrees', example: 90.0, required: false })
  currentHeading?: number;

  @ApiProperty({ description: 'Target timestamp to predict for (ms)', example: 1718000000000, required: false })
  targetTimestampMs?: number;
}

@ApiTags('Prediction')
@Controller('api/prediction')
export class PredictionController {
  constructor(private readonly grpcPredictionClient: GrpcPredictionClient) {}

  @Post('predict-next')
  @ApiOperation({ summary: 'Predict next location using AI STTF-Recommender model' })
  @ApiResponse({ status: 201, description: 'Return prediction results including predicted POI, coordinates, confidence, and intent' })
  async predictNext(@Body() request: PredictNextLocationDto) {
    const grpcRequest: PredictNextLocationRequest = {
      userId: request.userId,
      trajectory: request.trajectory,
      currentHeading: request.currentHeading ?? 0.0,
      targetTimestampMs: request.targetTimestampMs,
    };
    const response = await this.grpcPredictionClient.predictNextLocation(grpcRequest);
    return response;
  }
}
