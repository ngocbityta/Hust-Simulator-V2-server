import { Module } from '@nestjs/common';
import { ComputationController } from './computation.controller';
import { PlayerModule } from '../player/player.module';
import { SpatialModule } from '../spatial/spatial.module';
import { GrpcModule } from '../grpc/grpc.module';
import { AssistantModule } from '../assistant/assistant.module';

@Module({
  imports: [PlayerModule, SpatialModule, GrpcModule, AssistantModule],
  controllers: [ComputationController],
})
export class ComputationModule {}
