import { Module } from '@nestjs/common';
import { PredictionController } from './prediction.controller';
import { GrpcModule } from '../grpc/grpc.module';

@Module({
  imports: [GrpcModule],
  controllers: [PredictionController],
})
export class PredictionModule {}
