import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { GridCell, ISpatialService } from './spatial.interface';
import { RedisKey } from '../common/enums/redis-key.enum';

const METERS_PER_LAT = 111000;

@Injectable()
export class SpatialService implements ISpatialService {
  private readonly logger = new Logger(SpatialService.name);
  private readonly cellSize: number;

  private readonly METERS_PER_LAT = METERS_PER_LAT;
  private readonly METERS_PER_LNG: number;

  constructor(private configService: ConfigService) {
    this.cellSize = this.configService.get<number>('GRID_CELL_SIZE', 50);
    const avgLat = 21.003;
    this.METERS_PER_LNG = METERS_PER_LAT * Math.cos((avgLat * Math.PI) / 180);

    this.logger.log(
      `SpatialService initialized with cell size ${this.cellSize}m`,
    );
  }

  getGridCell(latitude: number, longitude: number): GridCell {
    const x = Math.floor((longitude * this.METERS_PER_LNG) / this.cellSize);
    const y = Math.floor((latitude * this.METERS_PER_LAT) / this.cellSize);
    return { x, y };
  }

  getCellKey(cell: GridCell): string {
    return `${cell.x}:${cell.y}`;
  }

  getAoiCells(centerCell: GridCell): GridCell[] {
    const cells: GridCell[] = [];
    for (let dx = -1; dx <= 1; dx++) {
      for (let dy = -1; dy <= 1; dy++) {
        cells.push({
          x: centerCell.x + dx,
          y: centerCell.y + dy,
        });
      }
    }
    return cells;
  }

  getCellChannel(cell: GridCell): string {
    return `${RedisKey.CELL_CHANNEL_PREFIX}${this.getCellKey(cell)}`;
  }
}
