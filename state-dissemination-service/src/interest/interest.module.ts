import { Module } from '@nestjs/common';
import { InterestService } from './interest.service';
import { IInterestService } from './interest.interface';
import { GrpcModule } from '../grpc/grpc.module';
import { SpatialModule } from '../spatial/spatial.module';

@Module({
  imports: [GrpcModule, SpatialModule],
  providers: [
    {
      provide: IInterestService,
      useClass: InterestService,
    },
  ],
  exports: [IInterestService],
})
export class InterestModule {}
