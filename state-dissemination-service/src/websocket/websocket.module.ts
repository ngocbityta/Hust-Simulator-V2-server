import { Module } from '@nestjs/common';
import { GameGateway } from './game.gateway';
import { PlayerModule } from '../player/player.module';
import { DisseminationService } from './dissemination.service';
import { GrpcModule } from '../grpc/grpc.module';

@Module({
    imports: [PlayerModule, GrpcModule],
    providers: [GameGateway, DisseminationService],
    exports: [GameGateway, DisseminationService],
})
export class WebsocketModule { }
