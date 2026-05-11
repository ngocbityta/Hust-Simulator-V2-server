import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { ScheduleModule } from '@nestjs/schedule';
import { GrpcModule } from './grpc/grpc.module';
import { PlayerModule } from './player/player.module';
import { RedisModule } from './redis/redis.module';
import { SpatialModule } from './spatial/spatial.module';
import { ComputationModule } from './computation/computation.module';
import { HeatmapModule } from './heatmap/heatmap.module';
import { IntentModule } from './intent/intent.module';

@Module({
  imports: [
    // Global config from .env
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: ['.env', '../.env'],
    }),

    // Scheduler for periodic tasks (heatmap aggregation)
    ScheduleModule.forRoot(),

    // Feature modules
    RedisModule,
    SpatialModule,
    GrpcModule,
    PlayerModule,
    ComputationModule,
    HeatmapModule,
    IntentModule,
  ],
})
export class AppModule {}
