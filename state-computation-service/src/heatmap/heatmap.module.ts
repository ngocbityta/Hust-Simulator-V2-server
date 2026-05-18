import { Module } from '@nestjs/common';
import { HeatmapService } from './heatmap.service';
import { SpatialModule } from '../spatial/spatial.module';
import { HeatmapController } from './heatmap.controller';

@Module({
  imports: [SpatialModule],
  controllers: [HeatmapController],
  providers: [HeatmapService],
  exports: [HeatmapService],
})
export class HeatmapModule {}
