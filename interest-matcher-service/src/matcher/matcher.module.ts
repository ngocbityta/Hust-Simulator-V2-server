import { Module } from '@nestjs/common';
import { MatcherController } from './matcher.controller';
import { MatcherService } from './matcher.service';
import { RedisModule } from '../redis/redis.module';
import { ZoneModule } from '../zone/zone.module';

@Module({
  imports: [RedisModule, ZoneModule],
  controllers: [MatcherController],
  providers: [MatcherService],
})
export class MatcherModule {}
