import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { GrpcModule } from './grpc/grpc.module';
import { WebsocketModule } from './websocket/websocket.module';
import { PlayerModule } from './player/player.module';
import { RedisModule } from './redis/redis.module';
import { SpatialModule } from './spatial/spatial.module';

@Module({
  imports: [
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
    PlayerModule,
  ],
})
export class AppModule { }
