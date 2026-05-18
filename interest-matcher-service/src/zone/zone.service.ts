import { Injectable, Logger, OnModuleInit } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

export interface ZoneConfig {
  zoneId: number;
  lngMin: number;
  lngMax: number;
  neighborZones: { zoneId: number; host: string; port: number }[];
}

/**
 * ZoneService — Spatial zone management for multi-broker Interest Matcher.
 *
 * The bản đồ BKHN is divided into longitude bands (zones).
 * Each Interest Matcher instance owns one zone.
 *
 * Zone boundaries (default, override via env):
 *   Zone 0: lng < ZONE_1_LNG  (105.840)
 *   Zone 1: ZONE_1_LNG ≤ lng < ZONE_2_LNG  (105.850)
 *   Zone 2: lng ≥ ZONE_2_LNG
 *
 * Border detection: a grid cell is considered a "border cell" if its
 * longitude centre is within BORDER_MARGIN_M meters of a zone boundary.
 * Border cells trigger inter-broker forwarding (VoroCast equivalent).
 */
@Injectable()
export class ZoneService implements OnModuleInit {
  private readonly logger = new Logger(ZoneService.name);

  private readonly zoneId: number;
  private readonly lngMin: number;
  private readonly lngMax: number;

  // Boundary longitudes between zones (shared across all instances)
  private readonly zone1Lng: number;
  private readonly zone2Lng: number;

  // Border margin in meters — cells within this distance of boundary get forwarded
  private readonly BORDER_MARGIN_DEG: number;

  // Cell size in metres (mirrors state-computation-service config)
  private readonly CELL_SIZE_M: number;

  // Parsed neighbor broker endpoints
  private readonly neighbors: { zoneId: number; host: string; port: number }[] = [];

  constructor(private readonly configService: ConfigService) {
    // Env vars come as strings — must parseInt to ensure numeric comparison
    this.zoneId = parseInt(
      this.configService.get<string>('ZONE_ID', '0'),
      10,
    );
    this.zone1Lng = parseFloat(
      this.configService.get<string>('ZONE_1_LNG', '105.84'),
    );
    this.zone2Lng = parseFloat(
      this.configService.get<string>('ZONE_2_LNG', '105.85'),
    );
    this.CELL_SIZE_M = parseFloat(
      this.configService.get<string>('GRID_CELL_SIZE', '50'),
    );

    // Compute zone boundaries for this instance
    this.lngMin = this.zoneId === 0 ? -Infinity : this.zoneId === 1 ? this.zone1Lng : this.zone2Lng;
    this.lngMax = this.zoneId === 0 ? this.zone1Lng : this.zoneId === 1 ? this.zone2Lng : Infinity;

    // Convert cell size to approximate degrees for border margin (1 cell wide)
    const mapCenterLat = parseFloat(
      this.configService.get<string>('MAP_CENTER_LAT', '21.003'),
    );
    const METERS_PER_LNG = 111000 * Math.cos((mapCenterLat * Math.PI) / 180);
    this.BORDER_MARGIN_DEG = this.CELL_SIZE_M / METERS_PER_LNG;

    // Parse NEIGHBOR_BROKERS env: "host1:port1:zoneId1,host2:port2:zoneId2"
    const neighborsEnv = this.configService.get<string>('NEIGHBOR_BROKERS', '');
    if (neighborsEnv) {
      neighborsEnv.split(',').forEach((entry) => {
        const parts = entry.trim().split(':');
        if (parts.length === 3) {
          this.neighbors.push({
            host: parts[0],
            port: parseInt(parts[1], 10),
            zoneId: parseInt(parts[2], 10),
          });
        }
      });
    }
  }


  onModuleInit() {
    this.logger.log(
      `ZoneService initialized — Zone ${this.zoneId} ` +
      `[lng ${this.lngMin === -Infinity ? '-∞' : this.lngMin} … ` +
      `${this.lngMax === Infinity ? '+∞' : this.lngMax}]`,
    );
    this.logger.log(`Neighbor brokers: ${JSON.stringify(this.neighbors)}`);
  }

  getZoneId(): number {
    return this.zoneId;
  }

  getNeighbors(): { zoneId: number; host: string; port: number }[] {
    return this.neighbors;
  }

  /**
   * Determine which zone(s) a publication at `entityLongitude` should reach.
   * Returns an array of zone IDs — normally just [this.zoneId], but also
   * includes neighbor zone IDs when the entity is at a border cell.
   */
  getTargetZones(entityLongitude: number): number[] {
    const zones: number[] = [this.zoneId];

    // Near lower boundary → also forward to left neighbor
    if (
      this.lngMin !== -Infinity &&
      entityLongitude - this.lngMin < this.BORDER_MARGIN_DEG
    ) {
      const leftNeighbor = this.neighbors.find((n) => n.zoneId === this.zoneId - 1);
      if (leftNeighbor) zones.push(leftNeighbor.zoneId);
    }

    // Near upper boundary → also forward to right neighbor
    if (
      this.lngMax !== Infinity &&
      this.lngMax - entityLongitude < this.BORDER_MARGIN_DEG
    ) {
      const rightNeighbor = this.neighbors.find((n) => n.zoneId === this.zoneId + 1);
      if (rightNeighbor) zones.push(rightNeighbor.zoneId);
    }

    return zones;
  }

  isBorderLongitude(longitude: number): boolean {
    const targets = this.getTargetZones(longitude);
    return targets.length > 1;
  }

  getNeighborByZoneId(zoneId: number): { zoneId: number; host: string; port: number } | undefined {
    return this.neighbors.find((n) => n.zoneId === zoneId);
  }
}
