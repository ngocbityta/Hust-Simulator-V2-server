import { Injectable, Logger, OnModuleInit, OnModuleDestroy, Inject } from '@nestjs/common';
import { WebSocket } from 'ws';
import Redis from 'ioredis';
import { ISpatialService } from '../spatial/spatial.interface';
import { RedisService } from '../redis/redis.service';
import { ISessionService } from './session.interface';
import { IInterestService } from '../interest/interest.interface';
import { RedisKey } from '../common/enums/redis-key.enum';
import { WsEvent } from '../common/enums/ws-event.enum';

@Injectable()
export class DisseminationService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(DisseminationService.name);
  
  private subClient: Redis;

  private clientCenterCell = new Map<WebSocket, string>();

  constructor(
    private readonly redisService: RedisService,
    @Inject(ISpatialService) private readonly spatialService: ISpatialService,
    @Inject(ISessionService) private readonly sessionService: ISessionService,
    @Inject(IInterestService) private readonly interestService: IInterestService,
  ) {}

  onModuleInit() {
    this.subClient = this.redisService.subClient;

    this.subClient.on('message', (channel, message) => {
      this.handleRedisMessage(channel, message);
    });
  }

  onModuleDestroy() {}

  async updateLocation(client: WebSocket, latitude: number, longitude: number) {
    const currentCell = this.spatialService.getGridCell(latitude, longitude);
    const currentCellKey = this.spatialService.getCellKey(currentCell);
    
    const lastCellKey = this.clientCenterCell.get(client);
    if (lastCellKey !== currentCellKey) {
      const aoiCells = this.spatialService.getAoiCells(currentCell);
      const aoiKeys = new Set(aoiCells.map(c => this.spatialService.getCellKey(c)));
      this.interestService.updateInterests(client, aoiKeys);
      this.clientCenterCell.set(client, currentCellKey);
    }
  }

  removeClient(client: WebSocket) {
    this.interestService.removeClient(client);
    this.clientCenterCell.delete(client);
  }

  private handleRedisMessage(channel: string, message: string) {
    const cellKey = channel.replace(RedisKey.CELL_CHANNEL_PREFIX, '');
    const clients = this.interestService.getClientsInCell(cellKey);

    if (clients && clients.size > 0) {
      try {
        const payload = JSON.parse(message);
        const wsMessage = JSON.stringify({
          event: WsEvent.USER_STATE_UPDATE,
          data: payload,
        });

        clients.forEach(client => {
          if (client.readyState === WebSocket.OPEN) {
            const clientUserId = this.sessionService.getUserId(client);
            if (clientUserId !== payload.userId) {
              client.send(wsMessage);
            }
          }
        });
      } catch (err) {
        this.logger.error('Failed to parse Redis message', err);
      }
    }
  }
}
