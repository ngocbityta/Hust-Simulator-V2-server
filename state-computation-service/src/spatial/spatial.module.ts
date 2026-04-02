import { Module } from '@nestjs/common';
import { SpatialService } from './spatial.service';

import { ISpatialService } from './spatial.interface';

@Module({
  providers: [
    {
      provide: ISpatialService,
      useClass: SpatialService,
    },
  ],
  exports: [ISpatialService],
})
export class SpatialModule {}
