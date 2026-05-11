import { Module } from '@nestjs/common';
import { PlayerService } from './player.service';
import { GrpcModule } from '../grpc/grpc.module';
import { UserStateGrpcController } from '../grpc/user-state.controller';
import { IntentModule } from '../intent/intent.module';

@Module({
  imports: [GrpcModule, IntentModule],
  controllers: [UserStateGrpcController],
  providers: [PlayerService],
  exports: [PlayerService],
})
export class PlayerModule {}
