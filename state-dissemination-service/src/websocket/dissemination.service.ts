import { Injectable, Logger, OnModuleInit, OnModuleDestroy } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { WebSocket } from 'ws';
import Redis from 'ioredis';
import { SpatialService, GridCell } from '../player/spatial.service';

@Injectable()
export class DisseminationService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(DisseminationService.name);
  
  private pubClient: Redis;
  private subClient: Redis;

  // Mapping from Cell Key -> Set of WebSocket clients subscribed to it
  private cellSubscriptions = new Map<string, Set<WebSocket>>();
  
  // Mapping from WebSocket client -> Set of Cell Keys they are currently subscribed to
  // (Used for efficient updates and cleanup on disconnect)
  private clientInterests = new Map<WebSocket, Set<string>>();

  constructor(
    private readonly configService: ConfigService,
    private readonly spatialService: SpatialService,
  ) {}

  onModuleInit() {
    const redisConfig = {
      host: this.configService.get<string>('REDIS_HOST', 'localhost'),
      port: this.configService.get<number>('REDIS_PORT', 6379),
      password: this.configService.get<string>('REDIS_PASSWORD'),
    };

    this.pubClient = new Redis(redisConfig);
    this.subClient = new Redis(redisConfig);

    // Subscribe to all game cell channels
    this.subClient.psubscribe('game:cell:*', (err) => {
      if (err) {
        this.logger.error('Failed to psubscribe to game cells', err);
      } else {
        this.logger.log('Subscribed to all game cell channels');
      }
    });

    this.subClient.on('pmessage', (pattern, channel, message) => {
      this.handleRedisMessage(channel, message);
    });
  }

  onModuleDestroy() {
    this.pubClient.quit();
    this.subClient.quit();
  }

  /**
   * Updates a client's position and manages their AOI subscriptions.
   */
  async updateLocation(client: WebSocket, userId: string, latitude: number, longitude: number, payload: any) {
    const currentCell = this.spatialService.getGridCell(latitude, longitude);
    const aoiCells = this.spatialService.getAoiCells(currentCell);
    const aoiKeys = new Set(aoiCells.map(c => this.spatialService.getCellKey(c)));

    // 1. Update Subscriptions (AOI management)
    this.manageSubscriptions(client, aoiKeys);

    // 2. Publish player's state to their current cell
    const channel = this.spatialService.getCellChannel(currentCell);
    const message = JSON.stringify({
      userId,
      ...payload,
      timestamp: Date.now(),
    });

    await this.pubClient.publish(channel, message);
  }

  /**
   * Removes all subscriptions for a client (called on disconnect).
   */
  removeClient(client: WebSocket) {
    const interests = this.clientInterests.get(client);
    if (interests) {
      interests.forEach(cellKey => {
        const clients = this.cellSubscriptions.get(cellKey);
        if (clients) {
          clients.delete(client);
          if (clients.size === 0) {
            this.cellSubscriptions.delete(cellKey);
          }
        }
      });
      this.clientInterests.delete(client);
    }
  }

  private manageSubscriptions(client: WebSocket, newAoiKeys: Set<string>) {
    const currentInterests = this.clientInterests.get(client) || new Set<string>();

    // Subscribe to new cells
    newAoiKeys.forEach(key => {
      if (!currentInterests.has(key)) {
        if (!this.cellSubscriptions.has(key)) {
          this.cellSubscriptions.set(key, new Set());
        }
        this.cellSubscriptions.get(key)!.add(client);
      }
    });

    // Unsubscribe from old cells
    currentInterests.forEach(key => {
      if (!newAoiKeys.has(key)) {
        const clients = this.cellSubscriptions.get(key);
        if (clients) {
          clients.delete(client);
          if (clients.size === 0) {
            this.cellSubscriptions.delete(key);
          }
        }
      }
    });

    // Update client's interest set
    this.clientInterests.set(client, newAoiKeys);
  }

  private handleRedisMessage(channel: string, message: string) {
    // Extract cell key from channel name: game:cell:x:y -> x:y
    const cellKey = channel.replace('game:cell:', '');
    const clients = this.cellSubscriptions.get(cellKey);

    if (clients && clients.size > 0) {
      const payload = JSON.parse(message);
      const wsMessage = JSON.stringify({
        event: 'user:state_update',
        data: payload,
      });

      clients.forEach(client => {
        if (client.readyState === 1) { // WebSocket.OPEN
          client.send(wsMessage);
        }
      });
    }
  }
}
