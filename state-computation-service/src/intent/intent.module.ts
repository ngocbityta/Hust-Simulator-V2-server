import { Module } from '@nestjs/common';
import { IntentService } from './intent.service';
import { RedisModule } from '../redis/redis.module';
import { GrpcModule } from '../grpc/grpc.module';
import { ConfigModule } from '@nestjs/config';
import { IIntentService } from './intent.interface';
import { HeuristicPredictor } from './predictors/heuristic.predictor';
import { AIPredictor } from './predictors/ai.predictor';

@Module({
  imports: [RedisModule, GrpcModule, ConfigModule],
  providers: [
    {
      provide: IIntentService,
      useClass: IntentService,
    },
    HeuristicPredictor,
    AIPredictor,
  ],
  exports: [IIntentService],
})
export class IntentModule {}
