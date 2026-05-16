import {
  Injectable,
  Logger,
  OnModuleInit,
  OnModuleDestroy,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Redis from 'ioredis';

/**
 * RedisService for Interest Matcher.
 *
 * - pubClient: used to push delivery messages into Redis Streams
 *   (one stream per dissemination node: "diss:{nodeId}:events")
 *
 * - interBrokerClient: used to push inter-broker forwarding messages into
 *   Redis Streams ("ib:zone:{zoneId}" consumed by neighbor brokers)
 *
 * - subClient: subscribes to this broker's inter-broker stream
 *   ("ib:zone:{this.zoneId}") to receive forwarded publications from neighbors
 */
@Injectable()
export class RedisService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(RedisService.name);

  public pubClient: Redis;
  public subClient: Redis;

  private readonly zoneId: number;
  private interBrokerConsumerRunning = false;

  constructor(private readonly configService: ConfigService) {
    this.zoneId = this.configService.get<number>('ZONE_ID', 0);
  }

  onModuleInit() {
    const redisConfig = {
      host: this.configService.get<string>('REDIS_HOST', 'localhost'),
      port: this.configService.get<number>('REDIS_PORT', 6379),
      password: this.configService.get<string>('REDIS_PASSWORD'),
    };

    this.pubClient = new Redis(redisConfig);
    this.subClient = new Redis(redisConfig);

    this.pubClient.on('connect', () => this.logger.log('Redis pubClient connected'));
    this.pubClient.on('error', (err) => this.logger.error('Redis pubClient error', err));
    this.subClient.on('connect', () => this.logger.log('Redis subClient connected'));
    this.subClient.on('error', (err) => this.logger.error('Redis subClient error', err));
  }

  async onModuleDestroy() {
    this.interBrokerConsumerRunning = false;
    await this.pubClient.quit();
    await this.subClient.quit();
  }

  /**
   * Deliver a state-update message to a dissemination node via Redis Stream.
   * Stream key: "diss:{dissNodeId}:events"
   * The state-dissemination-service reads from this stream via XREAD BLOCK.
   */
  async deliverToDissNode(dissNodeId: string, payload: string): Promise<void> {
    const streamKey = `diss:${dissNodeId}:events`;
    await this.pubClient.xadd(streamKey, '*', 'payload', payload);
    // Trim to last 1000 messages to prevent unbounded growth
    await this.pubClient.xtrim(streamKey, 'MAXLEN', '~', 1000);
  }

  /**
   * Forward a publication to a neighbor broker's inter-broker stream.
   * Stream key: "ib:zone:{neighborZoneId}"
   * The neighbor broker's consumeInterBrokerStream() reads from it.
   */
  async forwardToNeighborBroker(neighborZoneId: number, cellKey: string, payload: string): Promise<void> {
    const streamKey = `ib:zone:${neighborZoneId}`;
    await this.pubClient.xadd(streamKey, '*', 'cell_key', cellKey, 'payload', payload);
    await this.pubClient.xtrim(streamKey, 'MAXLEN', '~', 500);
  }

  /**
   * Start consuming this broker's inter-broker stream.
   * Runs a XREAD BLOCK loop — call once from MatcherService.onModuleInit().
   */
  async consumeInterBrokerStream(
    handler: (cellKey: string, payload: string) => Promise<void>,
  ): Promise<void> {
    const streamKey = `ib:zone:${this.zoneId}`;
    const groupName = `im-zone-${this.zoneId}-group`;
    const consumerName = `im-zone-${this.zoneId}-consumer`;

    // Create consumer group if not exists
    try {
      await this.subClient.xgroup('CREATE', streamKey, groupName, '$', 'MKSTREAM');
    } catch {
      // Group already exists — ignore BUSYGROUP error
    }

    this.interBrokerConsumerRunning = true;
    this.logger.log(`Listening on inter-broker stream: ${streamKey}`);

    const loop = async () => {
      while (this.interBrokerConsumerRunning) {
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
              const cellKey = fields[fields.indexOf('cell_key') + 1];
              const payload = fields[fields.indexOf('payload') + 1];

              if (cellKey && payload) {
                await handler(cellKey, payload);
              }

              // Acknowledge the message
              await this.subClient.xack(streamKey, groupName, msgId);
            }
          }
        } catch (err) {
          this.logger.error('Error reading inter-broker stream', err);
          await new Promise((r) => setTimeout(r, 1000));
        }
      }
    };

    // Run non-blocking in background
    void loop();
  }
}
