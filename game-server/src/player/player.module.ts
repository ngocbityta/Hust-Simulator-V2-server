import { Module } from '@nestjs/common';
import { PlayerService } from './player.service';
import { GrpcModule } from '../grpc/grpc.module';
import { PlayerStateGrpcController } from '../grpc/player-state.controller';

@Module({
    imports: [GrpcModule],
    controllers: [PlayerStateGrpcController],
    providers: [PlayerService],
    exports: [PlayerService],
})
export class PlayerModule { }
