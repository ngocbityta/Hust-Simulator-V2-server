import { Inject, Injectable, OnModuleInit, Logger } from '@nestjs/common';
import type { ClientGrpc } from '@nestjs/microservices';
import { Observable, firstValueFrom } from 'rxjs';
import { UserActivityState } from '../common/enums/user-activity-state.enum';

interface ComputationService {
  processUserMove(data: any): Observable<void>;
  processUserStateChange(data: any): Observable<void>;
}

interface UserStateService {
  notifyUserConnection(data: any): Observable<void>;
}

@Injectable()
export class GrpcComputationClient implements OnModuleInit {
  private readonly logger = new Logger(GrpcComputationClient.name);
  private computationService!: ComputationService;
  private userStateService!: UserStateService;

  constructor(
    @Inject('COMPUTATION_SERVICE') private readonly client: ClientGrpc,
  ) {}

  onModuleInit() {
    this.computationService =
      this.client.getService<ComputationService>('ComputationService');
    this.userStateService =
      this.client.getService<UserStateService>('UserStateService');
    this.logger.log('gRPC Computation client initialized');
  }

  async processUserMove(
    userId: string,
    position: { latitude: number; longitude: number },
    speed: number,
    heading: number,
    clientTimestamp?: number,
  ): Promise<void> {
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
  ): Promise<void> {
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

  async notifyUserConnection(
    userId: string,
    isConnected: boolean,
  ): Promise<void> {
    return firstValueFrom(
      this.userStateService.notifyUserConnection({
        userId,
        isConnected,
        timestamp: { millis: Date.now() },
      }),
    );
  }
}
