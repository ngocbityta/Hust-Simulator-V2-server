import { Module } from '@nestjs/common';
import { HeatmapDisseminationService } from './heatmap-dissemination.service';

@Module({
  providers: [HeatmapDisseminationService],
  exports: [HeatmapDisseminationService],
})
export class HeatmapModule {}
