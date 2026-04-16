import { Injectable, Logger } from '@nestjs/common';
import { WebSocket } from 'ws';
import { RedisService } from '../redis/redis.service';
import { IInterestService } from './interest.interface';
import { RedisKey } from '../common/enums/redis-key.enum';

@Injectable()
export class InterestService implements IInterestService {
  private readonly logger = new Logger(InterestService.name);

  private cellSubscriptions = new Map<string, Set<WebSocket>>();
  private clientInterests = new Map<WebSocket, Set<string>>();
  private nodeInterests = new Map<string, number>();

  constructor(private readonly redisService: RedisService) {}

  updateInterests(client: WebSocket, newAoiKeys: Set<string>) {
    if (!newAoiKeys) {
      this.logger.error('updateInterests called with undefined newAoiKeys');
      return;
    }
    let currentInterests = this.clientInterests.get(client);
    if (!currentInterests) {
      currentInterests = new Set<string>();
    }

    for (const key of newAoiKeys) {
      if (!currentInterests.has(key)) {
        const refCount = (this.nodeInterests.get(key) || 0) + 1;
        this.nodeInterests.set(key, refCount);

        if (refCount === 1) {
          const channel = `${RedisKey.CELL_CHANNEL_PREFIX}${key}`;
          this.redisService.subClient.subscribe(channel).catch((err) => {
            this.logger.error(`Failed to subscribe to ${channel}`, err);
          });
        }

        if (!this.cellSubscriptions.has(key)) {
          this.cellSubscriptions.set(key, new Set());
        }
        const cellSubs = this.cellSubscriptions.get(key);
        if (cellSubs) {
          cellSubs.add(client);
        }
      }
    }

    for (const key of currentInterests) {
      if (!newAoiKeys.has(key)) {
        this.removeClientInterestInCell(client, key);
      }
    }

    this.clientInterests.set(client, newAoiKeys);
  }

  getClientsInCell(cellKey: string): Set<WebSocket> | undefined {
    return this.cellSubscriptions.get(cellKey);
  }

  removeClient(client: WebSocket) {
    const interests = this.clientInterests.get(client);
    if (interests) {
      interests.forEach((key) => {
        this.removeClientInterestInCell(client, key);
      });
      this.clientInterests.delete(client);
    }
  }

  private removeClientInterestInCell(client: WebSocket, key: string) {
    const clients = this.cellSubscriptions.get(key);
    if (clients) {
      clients.delete(client);
      if (clients.size === 0) {
        this.cellSubscriptions.delete(key);
      }
    }

    const refCount = (this.nodeInterests.get(key) || 1) - 1;
    if (refCount <= 0) {
      this.nodeInterests.delete(key);
      const channel = `${RedisKey.CELL_CHANNEL_PREFIX}${key}`;
      this.redisService.subClient.unsubscribe(channel).catch((err) => {
        this.logger.error(`Failed to unsubscribe from ${channel}`, err);
      });
    } else {
      this.nodeInterests.set(key, refCount);
    }
  }
}
