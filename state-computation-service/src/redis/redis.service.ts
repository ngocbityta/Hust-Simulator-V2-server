import {
  Injectable,
  Logger,
  OnModuleDestroy,
  OnModuleInit,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Redis from 'ioredis';

declare module 'ioredis' {
  interface Redis {
    updatePlayerState(
      key: string,
      geoKey: string,
      fieldValuePairs: (string | number)[],
      timestamp: string,
      shouldGeoAdd: string,
      longitude: number | string,
      latitude: number | string,
      userId: string,
    ): Promise<number>;
  }
}

@Injectable()
export class RedisService implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(RedisService.name);

  public client: Redis;
  public pubClient: Redis;

  constructor(private readonly configService: ConfigService) {}

  onModuleInit() {
    const redisConfig = {
      host: this.configService.get<string>('REDIS_HOST', 'localhost'),
      port: this.configService.get<number>('REDIS_PORT', 6379),
      password: this.configService.get<string>('REDIS_PASSWORD'),
    };

    this.client = new Redis(redisConfig);
    this.pubClient = new Redis(redisConfig);

    this.client.on('connect', () => this.logger.log('Redis client connected'));
    this.pubClient.on('connect', () =>
      this.logger.log('Redis pubClient connected'),
    );

    this.client.defineCommand('updatePlayerState', {
      numberOfKeys: 2,
      lua: `
        local key = KEYS[1]
        local geoKey = KEYS[2]
        local timestamp = ARGV[1]
        local shouldGeoAdd = ARGV[2]
        local longitude = ARGV[3]
        local latitude = ARGV[4]
        local userId = ARGV[5]

        -- Arg list starts from index 6
        if #ARGV >= 7 then
            local hsetArgs = {key}
            for i = 6, #ARGV do
                table.insert(hsetArgs, ARGV[i])
            end
            table.insert(hsetArgs, "lastUpdate")
            table.insert(hsetArgs, timestamp)
            redis.call('HSET', unpack(hsetArgs))
        else
            redis.call('HSET', key, "lastUpdate", timestamp)
        end

        if shouldGeoAdd == 'true' then
            redis.call('GEOADD', geoKey, longitude, latitude, userId)
        end
        return 1
      `,
    });

    this.client.on('error', (err) =>
      this.logger.error('Redis client error', err),
    );
  }

  onModuleDestroy() {
    this.client.quit();
    this.pubClient.quit();
  }
}
