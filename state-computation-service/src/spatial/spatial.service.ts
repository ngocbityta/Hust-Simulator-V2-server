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

  // Zone boundaries (longitude) — mirrored in interest-matcher-service
  private readonly zone1Lng: number;
  private readonly zone2Lng: number;
  // Border margin in degrees (~1 cell width)
  private readonly borderMarginDeg: number;

  constructor(private configService: ConfigService) {
    this.cellSize = this.configService.get<number>('GRID_CELL_SIZE', 50);
    const avgLat = this.configService.get<number>('MAP_CENTER_LAT', 21.003);
    this.METERS_PER_LNG = METERS_PER_LAT * Math.cos((avgLat * Math.PI) / 180);

    // Zone boundary longitudes (must match interest-matcher-service env)
    this.zone1Lng = parseFloat(String(this.configService.get('ZONE_1_LNG', '105.84')));
    this.zone2Lng = parseFloat(String(this.configService.get('ZONE_2_LNG', '105.85')));
    // Border margin ≈ 1 cell width in degrees
    this.borderMarginDeg = this.cellSize / this.METERS_PER_LNG;

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

  getCellSize(): number {
    return this.cellSize;
  }

  getMetersPerLat(): number {
    return this.METERS_PER_LAT;
  }

  getMetersPerLng(): number {
    return this.METERS_PER_LNG;
  }

  /**
   * Returns the zone ID (0, 1 or 2) for a given longitude.
   * Zone boundaries mirror the interest-matcher-service configuration.
   */
  getZoneId(longitude: number): number {
    if (longitude < this.zone1Lng) return 0;
    if (longitude < this.zone2Lng) return 1;
    return 2;
  }

  /**
   * Returns true if the longitude is within one cell-width of a zone boundary.
   * Used by ComputationController — the Interest Matcher broker handles the
   * actual forwarding, but the publisher can log/debug border events.
   */
  isBorderLongitude(longitude: number): boolean {
    const distToZone1 = Math.abs(longitude - this.zone1Lng);
    const distToZone2 = Math.abs(longitude - this.zone2Lng);
    return distToZone1 < this.borderMarginDeg || distToZone2 < this.borderMarginDeg;
  }
}
