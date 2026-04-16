import {
  Injectable,
  Logger,
  OnModuleDestroy,
  OnModuleInit,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Redis from 'ioredis';

@Injectable()
export class RedisService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(RedisService.name);

  public subClient: Redis;

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
    await this.subClient.quit();
  }
}
