import { Controller, Get, Query } from '@nestjs/common';
import { ApiTags, ApiOperation, ApiResponse, ApiProperty } from '@nestjs/swagger';
import { HeatmapService } from './heatmap.service';
import { PredictiveHeatmapService } from './predictive-heatmap.service';

export class HeatmapCellDto {
  @ApiProperty({ description: 'Cell X index', example: 45 })
  cellX: number;

  @ApiProperty({ description: 'Cell Y index', example: 12 })
  cellY: number;

  @ApiProperty({ description: 'Count of online users in this cell', example: 5 })
  count: number;

  @ApiProperty({ description: 'Cell center latitude coordinate', example: 21.0042 })
  centerLat: number;

  @ApiProperty({ description: 'Cell center longitude coordinate', example: 105.8456 })
  centerLng: number;
}

export class HeatmapPayloadDto {
  @ApiProperty({ description: 'Creation timestamp in milliseconds', example: 1684400000000 })
  timestamp: number;

  @ApiProperty({ description: 'Total online users across the campus', example: 150 })
  totalOnline: number;

  @ApiProperty({ description: 'Array of active heatmap cells', type: [HeatmapCellDto] })
  cells: HeatmapCellDto[];
}

export class PredictiveHeatmapCellDto {
  @ApiProperty({ description: 'Cell X index', example: 45 })
  cellX: number;

  @ApiProperty({ description: 'Cell Y index', example: 12 })
  cellY: number;

  @ApiProperty({ description: 'Count of projected users heading to or at this cell', example: 3 })
  count: number;

  @ApiProperty({ description: 'Cell center latitude coordinate', example: 21.0042 })
  centerLat: number;

  @ApiProperty({ description: 'Cell center longitude coordinate', example: 105.8456 })
  centerLng: number;

  @ApiProperty({
    description: 'Map of user intent categories to their count inside this cell',
    example: { 'GOING_TO_LOCATION': 2, 'IDLE': 1 },
    type: 'object',
    additionalProperties: { type: 'number' }
  })
  intents: Record<string, number>;
}

export class PredictiveHeatmapPayloadDto {
  @ApiProperty({ description: 'Creation timestamp in milliseconds', example: 1684400000000 })
  timestamp: number;

  @ApiProperty({ description: 'Total online users simulated on the campus', example: 150 })
  totalOnline: number;

  @ApiProperty({ description: 'Array of projected predictive heatmap cells', type: [PredictiveHeatmapCellDto] })
  cells: PredictiveHeatmapCellDto[];
}

@ApiTags('Heatmap')
@Controller('api/heatmap')
export class HeatmapController {
  constructor(
    private readonly heatmapService: HeatmapService,
    private readonly predictiveHeatmapService: PredictiveHeatmapService,
  ) {}

  @Get('latest')
  @ApiOperation({ summary: 'Get the latest aggregated standard heatmap data' })
  @ApiResponse({ status: 200, description: 'Return the most recent heatmap cells and online count', type: HeatmapPayloadDto })
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

  @Get('latest-predictive')
  @ApiOperation({ summary: 'Get the latest aggregated predictive heatmap data' })
  @ApiResponse({ status: 200, description: 'Return the most recent predictive heatmap cells and intents', type: PredictiveHeatmapPayloadDto })
  async getLatestPredictiveHeatmap(@Query('targetTime') targetTime?: string) {
    if (targetTime) {
      const targetTimeMs = parseInt(targetTime, 10);
      if (!isNaN(targetTimeMs)) {
        const data = await this.predictiveHeatmapService.generateHeatmap(targetTimeMs);
        if (data) return data;
      }
    }

    const data = this.predictiveHeatmapService.getLatestHeatmap();
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
