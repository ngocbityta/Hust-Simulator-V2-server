import { Inject, Injectable, OnModuleInit, Logger } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { Observable, firstValueFrom } from 'rxjs';

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
        radius: number;
        metadata: Record<string, string>;
    }>;
}

interface SpatialTriggerEvent {
    playerId: string;
    zoneId: string;
    triggerType: number;
    timestamp: { millis: number };
}

interface StatusResponse {
    success: boolean;
    message: string;
}

interface ContextEngineService {
    checkPlayerZone(request: ZoneCheckRequest): Observable<ZoneCheckResponse>;
    reportSpatialTrigger(
        request: SpatialTriggerEvent,
    ): Observable<StatusResponse>;
}

@Injectable()
export class GrpcContextClient implements OnModuleInit {
    private readonly logger = new Logger(GrpcContextClient.name);
    private contextService!: ContextEngineService;

    constructor(
        @Inject('CONTEXT_SERVICE') private readonly client: ClientGrpc,
    ) { }

    onModuleInit() {
        this.contextService =
            this.client.getService<ContextEngineService>('ContextEngineService');
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

    async reportSpatialTrigger(
        event: SpatialTriggerEvent,
    ): Promise<StatusResponse> {
        return firstValueFrom(this.contextService.reportSpatialTrigger(event));
    }
}
