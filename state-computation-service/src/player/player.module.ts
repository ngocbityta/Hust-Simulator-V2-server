import { Module } from '@nestjs/common';
import { PlayerService } from './player.service';
import { GrpcModule } from '../grpc/grpc.module';
import { UserStateGrpcController } from '../grpc/user-state.controller';

@Module({
  imports: [GrpcModule],
  controllers: [UserStateGrpcController],
  providers: [PlayerService],
  exports: [PlayerService],
})
export class PlayerModule {}
