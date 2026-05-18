import { Module } from '@nestjs/common';
import { AssistantService } from './assistant.service';
import { RedisModule } from '../redis/redis.module';
import { HeatmapModule } from '../heatmap/heatmap.module';
import { SpatialModule } from '../spatial/spatial.module';
import { ConfigModule } from '@nestjs/config';
import { GrpcModule } from '../grpc/grpc.module';

@Module({
  imports: [RedisModule, HeatmapModule, SpatialModule, ConfigModule, GrpcModule],
  providers: [AssistantService],
  exports: [AssistantService],
})
export class AssistantModule {}
