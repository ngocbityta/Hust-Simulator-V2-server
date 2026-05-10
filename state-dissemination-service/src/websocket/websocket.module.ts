import { Module } from '@nestjs/common';
import { GameGateway } from './game.gateway';
import { SpatialModule } from '../spatial/spatial.module';
import { DisseminationService } from './dissemination.service';
import { SessionService } from './session.service';
import { InterestModule } from '../interest/interest.module';
import { GrpcModule } from '../grpc/grpc.module';
import { ISessionService } from './session.interface';
import { HeatmapModule } from '../heatmap/heatmap.module';

@Module({
  imports: [GrpcModule, SpatialModule, InterestModule, HeatmapModule],
  providers: [
    GameGateway,
    DisseminationService,
    {
      provide: ISessionService,
      useClass: SessionService,
    },
  ],
  exports: [GameGateway, DisseminationService, ISessionService],
})
export class WebsocketModule {}
