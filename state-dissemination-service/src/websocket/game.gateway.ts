import {
  WebSocketGateway,
  WebSocketServer,
  OnGatewayInit,
  OnGatewayConnection,
  OnGatewayDisconnect,
  SubscribeMessage,
  MessageBody,
  ConnectedSocket,
} from '@nestjs/websockets';
import { Logger, Inject } from '@nestjs/common';
import { Server, WebSocket } from 'ws';
import { UserActivityState } from '../common/enums/user-activity-state.enum';
import { DisseminationService } from './dissemination.service';
import { ISessionService } from './session.interface';
import { GrpcComputationClient } from '../grpc/computation.client';
import { HeatmapDisseminationService } from '../heatmap/heatmap-dissemination.service';
import { WsEvent } from '../common/enums/ws-event.enum';
import { ConfigService } from '@nestjs/config';
import * as jwt from 'jsonwebtoken';

@WebSocketGateway({
  cors: { origin: '*' },
  path: '/ws',
})
export class GameGateway
  implements OnGatewayInit, OnGatewayConnection, OnGatewayDisconnect
{
  private readonly logger = new Logger(GameGateway.name);
  private aliveClients = new Set<WebSocket>();

  @WebSocketServer()
  server!: Server;

  constructor(
    private readonly grpcComputationClient: GrpcComputationClient,
    private readonly disseminationService: DisseminationService,
    @Inject(ISessionService) private readonly sessionService: ISessionService,
    private readonly heatmapService: HeatmapDisseminationService,
    private readonly configService: ConfigService,
  ) {}

  afterInit(server: Server) {
    this.logger.log('WebSocket Gateway initialized');

    // Heartbeat: detect zombie connections (e.g. user lost network)
    const heartbeatInterval = this.configService.get<number>(
      'WS_HEARTBEAT_INTERVAL_MS',
      30000,
    );
    setInterval(() => {
      server.clients.forEach((client: WebSocket) => {
        if (!this.aliveClients.has(client)) {
          this.logger.warn('Terminating zombie WebSocket connection');
          client.terminate();
          return;
        }
        this.aliveClients.delete(client);
        client.ping();
      });
    }, heartbeatInterval);
  }

  handleConnection(client: WebSocket) {
    this.aliveClients.add(client);
    client.on('pong', () => {
      this.aliveClients.add(client);
    });
    this.logger.debug(
      `Client connected. Total sessions: ${this.sessionService.getSessionCount() + 1}`,
    );
  }

  handleDisconnect(client: WebSocket) {
    const userId = this.sessionService.getUserId(client);
    this.disseminationService.removeClient(client);
    this.heatmapService.removeSubscriber(client);
    if (userId) {
      this.sessionService.removeSession(client);

      // Check if user is still connected (e.g. from another tab)
      const currentClient = this.sessionService.getClient(userId);
      if (!currentClient) {
        this.logger.debug(
          `User ${userId} disconnected. Total sessions: ${this.sessionService.getSessionCount()}`,
        );
        this.grpcComputationClient
          .notifyUserConnection(userId, false)
          .catch((err) => {
            this.logger.error(
              `Failed to notify offline status for ${userId}`,
              err,
            );
          });
      } else {
        this.logger.debug(
          `User ${userId} disconnected from an old session, but a new session is active. Skipping offline notification.`,
        );
      }
    }
  }

  @SubscribeMessage(WsEvent.USER_JOIN)
  handleUserJoin(
    @ConnectedSocket() client: WebSocket,
    @MessageBody() data: { token: string },
  ) {
    if (!data.token) {
      this.logger.warn('Client attempted to join without a token');
      return {
        event: WsEvent.USER_ERROR,
        data: { message: 'Token is required' },
      };
    }

    try {
      const secret = this.configService.get<string>('JWT_SECRET');
      if (!secret) {
        throw new Error('JWT_SECRET is not configured in the environment');
      }
      const decoded = jwt.verify(data.token, secret) as jwt.JwtPayload;

      // eslint-disable-next-line @typescript-eslint/no-unsafe-assignment
      const userIdRaw = decoded.userId || decoded.sub;
      if (!userIdRaw) {
        throw new Error('User ID not found in token payload');
      }

      const userId = String(userIdRaw);

      this.sessionService.setSession(client, userId);
      this.logger.log(`User ${userId} joined successfully`);

      this.grpcComputationClient
        .notifyUserConnection(userId, true)
        .catch((err) => {
          this.logger.error(
            `Failed to notify online status for ${userId}`,
            err,
          );
        });

      return { event: WsEvent.USER_JOINED, data: { userId } };
    } catch (err) {
      this.logger.warn(
        `Invalid token connection attempt: ${(err as Error).message}`,
      );
      return {
        event: WsEvent.USER_ERROR,
        data: { message: 'Invalid or expired token' },
      };
    }
  }

  @SubscribeMessage(WsEvent.USER_MOVE)
  async handleUserMove(
    @ConnectedSocket() client: WebSocket,
    @MessageBody()
    data: {
      position: { latitude: number; longitude: number };
      speed: number;
      heading: number;
      clientTimestamp?: number;
    },
  ) {
    const userId = this.sessionService.getUserId(client);
    if (!userId) {
      this.logger.warn(`Move attempt from unauthenticated client.`);
      return { event: WsEvent.USER_ERROR, data: { message: 'Unauthorized' } };
    }

    if (
      !data ||
      !data.position ||
      typeof data.position.latitude !== 'number' ||
      typeof data.position.longitude !== 'number'
    ) {
      this.logger.warn(`Invalid move payload from user ${userId}`);
      return {
        event: WsEvent.USER_ERROR,
        data: { message: 'Invalid payload: position is required' },
      };
    }

    this.logger.debug(`Processing move for user ${userId}`);

    // Update AOI logically for this WS connection
    this.disseminationService.updateLocation(
      client,
      data.position.latitude,
      data.position.longitude,
    );

    // Forward computation request to ComputationService
    await this.grpcComputationClient.processUserMove(
      userId,
      data.position,
      data.speed,
      data.heading,
      data.clientTimestamp,
    );

    return { event: WsEvent.USER_MOVED_ACK, data: { timestamp: Date.now() } };
  }

  @SubscribeMessage(WsEvent.USER_STATE_CHANGE)
  async handleUserStateChange(
    @ConnectedSocket() client: WebSocket,
    @MessageBody()
    data: {
      activityState: UserActivityState;
      mapId?: string;
      eventId?: string;
      sessionData?: Record<string, unknown>;
      position?: { latitude: number; longitude: number };
    },
  ) {
    const userId = this.sessionService.getUserId(client);
    if (!userId) {
      return { event: WsEvent.USER_ERROR, data: { message: 'Unauthorized' } };
    }

    if (!data || !data.activityState) {
      this.logger.warn(`Invalid state change payload from user ${userId}`);
      return {
        event: WsEvent.USER_STATE_ERROR,
        data: { message: 'Invalid payload: activityState is required' },
      };
    }

    const validStates = Object.values(UserActivityState);
    if (!validStates.includes(data.activityState)) {
      this.logger.warn(`Invalid activity state: ${data.activityState}`);
      return {
        event: WsEvent.USER_STATE_ERROR,
        data: { message: 'Invalid activity state' },
      };
    }

    // Delegate state updates and context-syncing to Computation Service
    await this.grpcComputationClient.processUserStateChange(
      userId,
      data.activityState,
      data.mapId,
      data.eventId,
      data.sessionData,
      data.position,
    );

    return { event: WsEvent.USER_STATE_CHANGED_ACK, data: { status: 'ok' } };
  }

  @SubscribeMessage(WsEvent.HEATMAP_SUBSCRIBE)
  handleHeatmapSubscribe(@ConnectedSocket() client: WebSocket) {
    this.heatmapService.addSubscriber(client);
    this.logger.debug('Client subscribed to heatmap');
    return { event: WsEvent.HEATMAP_SUBSCRIBED, data: { status: 'ok' } };
  }

  @SubscribeMessage(WsEvent.HEATMAP_UNSUBSCRIBE)
  handleHeatmapUnsubscribe(@ConnectedSocket() client: WebSocket) {
    this.heatmapService.removeSubscriber(client);
    this.logger.debug('Client unsubscribed from heatmap');
    return { event: WsEvent.HEATMAP_UNSUBSCRIBED, data: { status: 'ok' } };
  }
}
