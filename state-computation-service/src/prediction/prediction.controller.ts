import { Controller, Post, Body } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiProperty } from '@nestjs/swagger';
import { GrpcPredictionClient, PredictNextLocationRequest } from '../grpc/prediction.client';

export class PredictNextLocationDto {
  @ApiProperty({ description: 'User ID UUID string', example: '123e4567-e89b-12d3-a456-426614174000' })
  userId: string;

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
  @ApiOperation({ summary: 'Predict next location using AI Logistic Regression model' })
  @ApiResponse({ status: 201, description: 'Return prediction results including predicted POI, coordinates, confidence, and intent' })
  async predictNext(@Body() request: PredictNextLocationDto) {
    const grpcRequest: PredictNextLocationRequest = {
      userId: request.userId,
      currentHeading: request.currentHeading ?? 0.0,
      targetTimestampMs: request.targetTimestampMs,
    };
    const response = await this.grpcPredictionClient.predictNextLocation(grpcRequest);
    return response;
  }
}
