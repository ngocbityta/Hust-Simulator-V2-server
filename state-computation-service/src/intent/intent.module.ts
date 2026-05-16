import { Module } from '@nestjs/common';
import { IntentService } from './intent.service';
import { RedisModule } from '../redis/redis.module';
import { GrpcModule } from '../grpc/grpc.module';
import { ConfigModule } from '@nestjs/config';
import { IIntentService } from './intent.interface';
import { HeuristicPredictor } from './predictors/heuristic.predictor';
import { AIPredictor } from './predictors/ai.predictor';
import { TrajectoryService } from './trajectory.service';

@Module({
  imports: [RedisModule, GrpcModule, ConfigModule],
  providers: [
    {
      provide: IIntentService,
      useClass: IntentService,
    },
    HeuristicPredictor,
    AIPredictor,
    TrajectoryService,
  ],
  exports: [IIntentService, TrajectoryService],
})
export class IntentModule {}
