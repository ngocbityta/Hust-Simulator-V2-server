import { Module } from '@nestjs/common';
import { ZoneService } from './zone.service';

@Module({
  providers: [ZoneService],
  exports: [ZoneService],
})
export class ZoneModule {}
