import { Module } from '@nestjs/common';
import { IntentService } from './intent.service';
import { RedisModule } from '../redis/redis.module';
import { GrpcModule } from '../grpc/grpc.module';
import { IIntentService } from './intent.interface';

@Module({
  imports: [RedisModule, GrpcModule],
  providers: [
    {
      provide: IIntentService,
      useClass: IntentService,
    },
  ],
  exports: [IIntentService],
})
export class IntentModule {}
