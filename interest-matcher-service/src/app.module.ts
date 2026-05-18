import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { MatcherModule } from './matcher/matcher.module';
import { RedisModule } from './redis/redis.module';
import { ZoneModule } from './zone/zone.module';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      envFilePath: ['.env', '../.env'],
    }),
    RedisModule,
    ZoneModule,
    MatcherModule,
  ],
})
export class AppModule {}
