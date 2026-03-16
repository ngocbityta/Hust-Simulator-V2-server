import { Module } from '@nestjs/common';
import { InterestService } from './interest.service';
import { RedisModule } from '../redis/redis.module';
import { IInterestService } from './interest.interface';

@Module({
  imports: [RedisModule],
  providers: [
    {
      provide: IInterestService,
      useClass: InterestService,
    },
  ],
  exports: [IInterestService],
})
export class InterestModule {}
