import { Module } from '@nestjs/common';
import { HeatmapService } from './heatmap.service';
import { SpatialModule } from '../spatial/spatial.module';
import { HeatmapController } from './heatmap.controller';
import { IntentModule } from '../intent/intent.module';
import { PredictiveHeatmapService } from './predictive-heatmap.service';

@Module({
  imports: [SpatialModule, IntentModule],
  controllers: [HeatmapController],
  providers: [HeatmapService, PredictiveHeatmapService],
  exports: [HeatmapService, PredictiveHeatmapService],
})
export class HeatmapModule {}
