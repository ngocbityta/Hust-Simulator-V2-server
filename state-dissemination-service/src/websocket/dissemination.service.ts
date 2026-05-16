import {
  Injectable,
  Logger,
  OnModuleInit,
  OnModuleDestroy,
  Inject,
} from '@nestjs/common';
import { WebSocket } from 'ws';
import { ISpatialService } from '../spatial/spatial.interface';
import { RedisService } from '../redis/redis.service';
import { ISessionService } from './session.interface';
import { IInterestService } from '../interest/interest.interface';
import { WsEvent } from '../common/enums/ws-event.enum';
import { ConfigService } from '@nestjs/config';

@Injectable()
export class DisseminationService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(DisseminationService.name);

  // Unique node ID — used as stream key suffix and broker SUBSCRIBE identifier
  private readonly nodeId: string;

  private clientCenterCell = new Map<WebSocket, string>();

  constructor(
    private readonly redisService: RedisService,
    @Inject(ISpatialService) private readonly spatialService: ISpatialService,
    @Inject(ISessionService) private readonly sessionService: ISessionService,
    @Inject(IInterestService) private readonly interestService: IInterestService,
    private readonly configService: ConfigService,
  ) {
    this.nodeId = this.configService.get<string>(
      'DISS_NODE_ID',
      process.env.HOSTNAME ?? 'diss-default',
    );
  }

  onModuleInit() {
    // Start consuming the Redis Stream: "diss:{nodeId}:events"
    // Interest Matcher brokers push state-update messages into this stream.
    void this.redisService.consumeDeliveryStream(
      this.nodeId,
      (payload) => this.handleStreamMessage(payload),
    );
    this.logger.log(`DisseminationService started (nodeId: ${this.nodeId})`);
  }

  onModuleDestroy() { }

  updateLocation(client: WebSocket, latitude: number, longitude: number) {
    const currentCell = this.spatialService.getGridCell(latitude, longitude);
    const currentCellKey = this.spatialService.getCellKey(currentCell);

    const lastCellKey = this.clientCenterCell.get(client);
    if (lastCellKey === currentCellKey) return;

    const aoiCells = this.spatialService.getAoiCells(currentCell);

    // Build longitude map for zone routing in InterestService
    const aoiCellLngMap = new Map<string, number>();
    const aoiKeys = new Set<string>();
    for (const cell of aoiCells) {
      const key = this.spatialService.getCellKey(cell);
      const lng = this.spatialService.getLongitudeFromX(cell.x);
      aoiKeys.add(key);
      aoiCellLngMap.set(key, lng);
    }

    this.interestService.updateInterests(client, aoiKeys, aoiCellLngMap);
    this.clientCenterCell.set(client, currentCellKey);
  }

  removeClient(client: WebSocket) {
    this.interestService.removeClient(client);
    this.clientCenterCell.delete(client);
  }

  private handleStreamMessage(rawPayload: string) {
    try {
      const payload = JSON.parse(rawPayload) as {
        userId: string;
        cellKey?: string;
        [key: string]: unknown;
      };

      const cellKey = payload.cellKey as string | undefined;
      if (!cellKey) {
        this.logger.warn('Stream message missing cellKey — dropped', rawPayload.substring(0, 100));
        return;
      }

      // Find local WS clients subscribed to this specific cell.
      const targets = this.interestService.getClientsInCell(cellKey);
      if (!targets || targets.size === 0) return;

      const wsMessage = JSON.stringify({
        event: WsEvent.USER_STATE_UPDATE,
        data: payload,
      });

      targets.forEach((client) => {
        if (client.readyState === WebSocket.OPEN) {
          const clientUserId = this.sessionService.getUserId(client);
          // Do not echo back to the originating user
          if (clientUserId !== payload.userId) {
            client.send(wsMessage);
          }
        }
      });
    } catch (err) {
      this.logger.error('Failed to parse stream message', err);
    }
  }
}

