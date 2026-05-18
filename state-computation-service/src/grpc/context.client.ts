import { Inject, Injectable, OnModuleInit, Logger } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { Observable, firstValueFrom } from 'rxjs';
import { GrpcService } from '../common/enums/grpc.enum';

// Interfaces matching context.proto ContextEngineService
interface ZoneCheckRequest {
  playerId: string;
  position: { latitude: number; longitude: number };
}

interface ZoneCheckResponse {
  zones: Array<{
    zoneId: string;
    name: string;
    type: string;
    center: { latitude: number; longitude: number };
    coordinates: string;
    metadata: Record<string, string>;
  }>;
}

interface SpatialTriggerEvent {
  playerId: string;
  zoneId: string;
  triggerType: number;
  timestamp: { millis: number };
}

interface UpdatePlayerStateRequest {
  playerId: string;
  position: { latitude: number; longitude: number };
  activityState: string;
  mapId: string;
  eventId: string;
  timestamp: { millis: number };
}

interface ActiveEventsRequest {
  playerId: string;
}

export interface ContextEvent {
  eventId: string;
  playerId: string;
  eventType: string;
  title: string;
  description: string;
  payload: Record<string, string>;
  timestamp: { millis: number };
}

export interface ActiveEventsResponse {
  events: ContextEvent[];
}

export interface GetHistoricalDensityRequest {
  cellX: number;
  cellY: number;
  sinceTimestampMs: number;
}

export interface GetHistoricalDensityResponse {
  averageCount: number;
}

interface ContextEngineService {
  checkPlayerZone(request: ZoneCheckRequest): Observable<ZoneCheckResponse>;
  reportSpatialTrigger(
    request: SpatialTriggerEvent,
  ): Observable<Record<string, never>>;
  updatePlayerState(
    request: UpdatePlayerStateRequest,
  ): Observable<Record<string, never>>;
  getActiveEvents(
    request: ActiveEventsRequest,
  ): Observable<ActiveEventsResponse>;
  getHistoricalDensity(
    request: GetHistoricalDensityRequest,
  ): Observable<GetHistoricalDensityResponse>;
}

@Injectable()
export class GrpcContextClient implements OnModuleInit {
  private readonly logger = new Logger(GrpcContextClient.name);
  private contextService!: ContextEngineService;

  constructor(@Inject('CONTEXT_SERVICE') private readonly client: ClientGrpc) {}

  onModuleInit() {
    this.contextService = this.client.getService<ContextEngineService>(
      GrpcService.CONTEXT_ENGINE_SERVICE,
    );
    this.logger.log('gRPC Context Engine client initialized');
  }

  async checkPlayerZone(
    playerId: string,
    latitude: number,
    longitude: number,
  ): Promise<ZoneCheckResponse> {
    return firstValueFrom(
      this.contextService.checkPlayerZone({
        playerId,
        position: { latitude, longitude },
      }),
    );
  }

  async reportSpatialTrigger(event: SpatialTriggerEvent): Promise<void> {
    await firstValueFrom(this.contextService.reportSpatialTrigger(event));
  }

  async updatePlayerState(request: UpdatePlayerStateRequest): Promise<void> {
    await firstValueFrom(this.contextService.updatePlayerState(request));
  }

  async getActiveEvents(playerId: string): Promise<ActiveEventsResponse> {
    return firstValueFrom(this.contextService.getActiveEvents({ playerId }));
  }

  async getHistoricalDensity(
    cellX: number,
    cellY: number,
    sinceTimestampMs: number,
  ): Promise<GetHistoricalDensityResponse> {
    return firstValueFrom(
      this.contextService.getHistoricalDensity({
        cellX,
        cellY,
        sinceTimestampMs,
      }),
    );
  }
}
