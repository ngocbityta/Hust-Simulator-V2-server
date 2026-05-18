import {
  Injectable,
  Logger,
  OnModuleDestroy,
  OnModuleInit,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Redis from 'ioredis';

/**
 * RedisService (state-dissemination-service).
 *
 * - subClient: dedicated connection for consuming the Redis Stream
 *   "diss:{nodeId}:events" that Interest Matcher brokers write to.
 *   Uses XREADGROUP BLOCK for efficient, low-latency delivery.
 *
 * Legacy subClient is kept for backward compatibility but is no longer
 * used for message routing (Interest Matcher handles that now).
 */
@Injectable()
export class RedisService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(RedisService.name);

  /** Dedicated client for Redis Stream consumption (XREADGROUP BLOCK) */
  public subClient: Redis;

  private streamConsumerRunning = false;

  constructor(private readonly configService: ConfigService) {}

  onModuleInit() {
    const redisConfig = {
      host: this.configService.get<string>('REDIS_HOST', 'localhost'),
      port: this.configService.get<number>('REDIS_PORT', 6379),
      password: this.configService.get<string>('REDIS_PASSWORD'),
    };

    this.subClient = new Redis(redisConfig);

    this.subClient.on('connect', () =>
      this.logger.log('Redis subClient connected'),
    );
    this.subClient.on('error', (err) =>
      this.logger.error('Redis subClient error', err),
    );
  }

  async onModuleDestroy() {
    this.streamConsumerRunning = false;
    await this.subClient.quit();
  }

  /**
   * Start consuming the Redis Stream that Interest Matcher brokers write to.
   * Stream key: "diss:{nodeId}:events"
   *
   * Each message has fields: { payload: string (JSON) }
   *
   * This replaces the old Redis Pub/Sub `on('message')` listener.
   * Call once from DisseminationService.onModuleInit().
   */
  async consumeDeliveryStream(
    nodeId: string,
    handler: (payload: string) => void,
  ): Promise<void> {
    const streamKey = `diss:${nodeId}:events`;
    const groupName = `diss-${nodeId}-group`;
    const consumerName = `diss-${nodeId}-consumer`;

    // Create consumer group if not exists ($ = only new messages)
    try {
      await this.subClient.xgroup('CREATE', streamKey, groupName, '$', 'MKSTREAM');
      this.logger.log(`Created consumer group for stream: ${streamKey}`);
    } catch {
      // BUSYGROUP error means group already exists — that's fine
    }

    this.streamConsumerRunning = true;
    this.logger.log(`Consuming Redis Stream: ${streamKey}`);

    const loop = async () => {
      while (this.streamConsumerRunning) {
        try {
          const results = await this.subClient.xreadgroup(
            'GROUP', groupName, consumerName,
            'COUNT', '100',
            'BLOCK', '2000',
            'STREAMS', streamKey, '>',
          ) as Array<[string, Array<[string, string[]]>]> | null;

          if (!results) continue;

          for (const [, messages] of results) {
            for (const [msgId, fields] of messages) {
              const payloadIdx = fields.indexOf('payload');
              if (payloadIdx !== -1) {
                handler(fields[payloadIdx + 1]);
              }
              // Acknowledge so message won't be re-delivered
              await this.subClient.xack(streamKey, groupName, msgId);
            }
          }
        } catch (err) {
          if (this.streamConsumerRunning) {
            this.logger.error('Error reading delivery stream', err);
            await new Promise((r) => setTimeout(r, 1000));
          }
        }
      }
    };

    // Non-blocking background loop
    void loop();
  }
}

