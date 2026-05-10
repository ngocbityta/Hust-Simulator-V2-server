import { Module } from '@nestjs/common';
import { HeatmapService } from './heatmap.service';
import { SpatialModule } from '../spatial/spatial.module';

@Module({
  imports: [SpatialModule],
  providers: [HeatmapService],
  exports: [HeatmapService],
})
export class HeatmapModule {}
