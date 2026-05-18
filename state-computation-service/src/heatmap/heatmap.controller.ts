import { Controller, Get } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse } from '@nestjs/swagger';
import { HeatmapService } from './heatmap.service';

@ApiTags('Heatmap')
@Controller('api/heatmap')
export class HeatmapController {
  constructor(private readonly heatmapService: HeatmapService) {}

  @Get('latest')
  @ApiOperation({ summary: 'Get the latest aggregated heatmap data' })
  @ApiResponse({ status: 200, description: 'Return the most recent heatmap cells and online count' })
  @ApiResponse({ status: 404, description: 'Heatmap data has not been generated yet' })
  getLatestHeatmap() {
    const data = this.heatmapService.getLatestHeatmap();
    if (!data) {
      return {
        timestamp: Date.now(),
        totalOnline: 0,
        cells: [],
        message: 'No data yet'
      };
    }
    return data;
  }
}
