import { Inject, Injectable, OnModuleInit, Logger } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { Observable, firstValueFrom } from 'rxjs';
import { UserActivityState } from '../common/enums/user-activity-state.enum';

interface ComputationService {
  processUserMove(data: any): Observable<{ success: boolean; message: string }>;
  processUserStateChange(
    data: any,
  ): Observable<{ success: boolean; message: string }>;
}

@Injectable()
export class GrpcComputationClient implements OnModuleInit {
  private readonly logger = new Logger(GrpcComputationClient.name);
  private computationService!: ComputationService;

  constructor(
    @Inject('COMPUTATION_SERVICE') private readonly client: ClientGrpc,
  ) {}

  onModuleInit() {
    this.computationService =
      this.client.getService<ComputationService>('ComputationService');
    this.logger.log('gRPC Computation client initialized');
  }

  async processUserMove(
    userId: string,
    position: { latitude: number; longitude: number },
    speed: number,
    heading: number,
    clientTimestamp?: number,
  ): Promise<{ success: boolean; message: string }> {
    return firstValueFrom(
      this.computationService.processUserMove({
        userId,
        position,
        speed,
        heading,
        clientTimestamp,
      }),
    );
  }

  async processUserStateChange(
    userId: string,
    activityState: UserActivityState,
    mapId?: string,
    eventId?: string,
    sessionData?: Record<string, unknown>,
    position?: { latitude: number; longitude: number },
  ): Promise<{ success: boolean; message: string }> {
    return firstValueFrom(
      this.computationService.processUserStateChange({
        userId,
        activityState,
        mapId: mapId || '',
        eventId: eventId || '',
        sessionData: sessionData ? JSON.stringify(sessionData) : '',
        position,
      }),
    );
  }
}
