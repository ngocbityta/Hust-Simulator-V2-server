import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { GrpcModule } from './grpc/grpc.module';
import { WebsocketModule } from './websocket/websocket.module';
import { RedisModule } from './redis/redis.module';
import { SpatialModule } from './spatial/spatial.module';

import { PrometheusModule } from '@willsoto/nestjs-prometheus';

@Module({
  imports: [
    // Prometheus Metrics
    PrometheusModule.register(),
    // Global config from .env
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: ['.env', '../.env'],
    }),

    // Feature modules
    RedisModule,
    SpatialModule,
    GrpcModule,
    WebsocketModule,
  ],
})
export class AppModule {}
