import {
  Injectable,
  Logger,
  OnModuleInit,
  OnModuleDestroy,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { WebSocket } from 'ws';
import Redis from 'ioredis';
import { RedisKey } from '../common/enums/redis-key.enum';
import { WsEvent } from '../common/enums/ws-event.enum';

@Injectable()
export class HeatmapDisseminationService
  implements OnModuleInit, OnModuleDestroy
{
  private readonly logger = new Logger(HeatmapDisseminationService.name);

  /** Clients that have opted-in to receive heatmap updates */
  private subscribers = new Set<WebSocket>();

  /** Dedicated Redis subscriber for the heatmap channel */
  private subClient: Redis;

  constructor(private readonly configService: ConfigService) {}

  onModuleInit() {
    // Create a dedicated Redis connection for heatmap subscription
    // (separate from the cell-channel subscriber in DisseminationService)
    this.subClient = new Redis({
      host: this.configService.get<string>('REDIS_HOST', 'localhost'),
      port: this.configService.get<number>('REDIS_PORT', 6379),
      password: this.configService.get<string>('REDIS_PASSWORD'),
    });

    this.subClient.on('connect', () => {
      this.logger.log('Heatmap Redis subscriber connected');
    });

    this.subClient.on('error', (err) => {
      this.logger.error('Heatmap Redis subscriber error', err);
    });

    // Subscribe to the heatmap channel
    this.subClient.subscribe(RedisKey.HEATMAP_CHANNEL).catch((err) => {
      this.logger.error('Failed to subscribe to heatmap channel', err);
    });

    this.subClient.on('message', (channel, message) => {
      if (channel === (RedisKey.HEATMAP_CHANNEL as string)) {
        this.broadcastHeatmap(message);
      }
    });

    this.logger.log('HeatmapDisseminationService initialized');
  }

  async onModuleDestroy() {
    await this.subClient.quit();
  }

  addSubscriber(client: WebSocket) {
    this.subscribers.add(client);
    this.logger.debug(
      `Heatmap subscriber added. Total: ${this.subscribers.size}`,
    );
  }

  removeSubscriber(client: WebSocket) {
    this.subscribers.delete(client);
  }

  getSubscriberCount(): number {
    return this.subscribers.size;
  }

  private broadcastHeatmap(message: string) {
    if (this.subscribers.size === 0) return;

    try {
      const data = JSON.parse(message) as unknown;
      const wsMessage = JSON.stringify({
        event: WsEvent.HEATMAP_UPDATE,
        data,
      });

      let sentCount = 0;
      for (const client of this.subscribers) {
        if (client.readyState === WebSocket.OPEN) {
          client.send(wsMessage);
          sentCount++;
        } else {
          // Auto-cleanup stale connections
          this.subscribers.delete(client);
        }
      }

      this.logger.debug(
        `Heatmap broadcast to ${sentCount}/${this.subscribers.size} subscribers`,
      );
    } catch (err) {
      this.logger.error('Failed to broadcast heatmap', err);
    }
  }
}
