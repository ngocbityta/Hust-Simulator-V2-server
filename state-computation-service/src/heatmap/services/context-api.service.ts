import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';

export interface Poi {
  name: string;
  lat: number;
  lng: number;
}

export interface ActiveEvent {
  buildingId?: string;
  name: string;
  estimatedParticipants?: number;
  startTime?: string;
  endTime?: string;
}

@Injectable()
export class ContextApiService {
  private readonly logger = new Logger(ContextApiService.name);

  constructor(private readonly configService: ConfigService) {}

  private getHostUrl(): string {
    const host = this.configService.get<string>('CONTEXT_SERVICE_HOST', 'localhost');
    const port = this.configService.get<number>('CONTEXT_SERVICE_REST_PORT', 8080);
    return `http://${host}:${port}`;
  }

  async loadPois(): Promise<Map<string, Poi>> {
    const poisMap = new Map<string, Poi>();
    try {
      const res = await fetch(`${this.getHostUrl()}/api/buildings/active`);
      if (res.ok) {
        const buildings = await res.json();
        for (const b of buildings) {
          poisMap.set(b.id, { name: b.name, lat: b.centroidLat, lng: b.centroidLng });
        }
        this.logger.log(`Loaded ${poisMap.size} buildings for POIs.`);
      }
    } catch (err) {
      this.logger.warn(`Could not load buildings for POI map: ${err}`);
    }
    return poisMap;
  }

  async fetchActiveEvents(targetTimestampMs: number): Promise<ActiveEvent[]> {
    try {
      const res = await fetch(`${this.getHostUrl()}/api/dashboard/stats?timeRange=24h`);
      if (res.ok) {
        const data = await res.json();
        return data.eventsTimeline || [];
      }
    } catch (err) {
      this.logger.warn(`Failed to fetch active events: ${err}`);
    }
    return [];
  }
}
