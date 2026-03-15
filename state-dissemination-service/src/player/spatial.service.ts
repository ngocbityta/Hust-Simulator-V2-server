import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

export interface GridCell {
  x: number;
  y: number;
}

@Injectable()
export class SpatialService {
  private readonly logger = new Logger(SpatialService.name);
  private readonly cellSize: number; // in meters

  // Constant for HUST Latitude (approx 21.003° N)
  // 1 degree latitude ~= 111,000 meters
  // 1 degree longitude ~= 111,000 * cos(latitude)
  private readonly METERS_PER_LAT = 111000;
  private readonly METERS_PER_LNG: number;

  constructor(private configService: ConfigService) {
    this.cellSize = this.configService.get<number>('GRID_CELL_SIZE', 50);
    const avgLat = 21.003; 
    this.METERS_PER_LNG = 111000 * Math.cos((avgLat * Math.PI) / 180);
    
    this.logger.log(`SpatialService initialized with cell size ${this.cellSize}m`);
  }

  /**
   * Converts GPS coordinates to grid cell coordinates.
   */
  getGridCell(latitude: number, longitude: number): GridCell {
    // We use a simple projection relative to (0,0) for the grid indices.
    // Since we only care about relative cell changes in a local area,
    // large scale projection distortion isn't an issue.
    const x = Math.floor((longitude * this.METERS_PER_LNG) / this.cellSize);
    const y = Math.floor((latitude * this.METERS_PER_LAT) / this.cellSize);
    return { x, y };
  }

  /**
   * Returns a unique string key for a grid cell.
   */
  getCellKey(cell: GridCell): string {
    return `${cell.x}:${cell.y}`;
  }

  /**
   * Returns all 9 cells in the $3 \times 3$ AOI around a target cell.
   */
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

  /**
   * Returns the Redis channel name for a specific cell.
   */
  getCellChannel(cell: GridCell): string {
    return `game:cell:${this.getCellKey(cell)}`;
  }
}
