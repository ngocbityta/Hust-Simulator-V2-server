import {
  Injectable,
  Logger,
  OnModuleInit,
  OnModuleDestroy,
} from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as grpc from '@grpc/grpc-js';
import * as protoLoader from '@grpc/proto-loader';
import { join } from 'path';

interface InterestMatcherGrpc {
  Publish(
    req: { cellKey: string; payload: string; entityLongitude: number },
    cb: (err: any, res: { success: boolean }) => void,
  ): void;
}

/**
 * GrpcInterestMatcherClient — routes PUBLISH calls to the correct
 * Interest Matcher broker based on the entity's longitude (zone).
 *
 * Matches the SPS paper: publisher determines which broker to send to;
 * the broker handles forwarding to neighbor zones at borders.
 *
 * Config env (state-computation-service):
 *   IM_ZONE_0_HOST  (default: interest-matcher-0)
 *   IM_ZONE_1_HOST  (default: interest-matcher-1)
 *   IM_ZONE_2_HOST  (default: interest-matcher-2)
 *   IM_BASE_PORT    (default: 4000, zone N → port 4000+N)
 *   ZONE_1_LNG      (default: 105.84)
 *   ZONE_2_LNG      (default: 105.85)
 */
@Injectable()
export class GrpcInterestMatcherClient implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(GrpcInterestMatcherClient.name);

  private clients: InterestMatcherGrpc[] = [];
  private readonly zone1Lng: number;
  private readonly zone2Lng: number;
  private readonly numZones: number;

  constructor(private readonly configService: ConfigService) {
    this.zone1Lng = parseFloat(String(this.configService.get('ZONE_1_LNG', '105.84')));
    this.zone2Lng = parseFloat(String(this.configService.get('ZONE_2_LNG', '105.85')));
    this.numZones = parseInt(
      String(this.configService.get('IM_NUM_ZONES', '3')),
      10,
    );
  }

  onModuleInit() {
    const packageDef = protoLoader.loadSync(
      join(__dirname, '../../../proto/interest-matcher.proto'),
      { keepCase: false, longs: String, enums: String, defaults: true, oneofs: true },
    );
    const proto = grpc.loadPackageDefinition(packageDef) as any;
    const MatcherClass = proto.hustsimulator.interest_matcher.InterestMatcher;

    const basePort = parseInt(
      String(this.configService.get('IM_BASE_PORT', '4000')),
      10,
    );
    const defaultHosts = Array.from(
      { length: this.numZones },
      (_, z) => `interest-matcher-${z}`,
    );

    for (let z = 0; z < this.numZones; z++) {
      const host = this.configService.get<string>(
        `IM_ZONE_${z}_HOST`,
        defaultHosts[z],
      );
      const port = basePort + z;
      const url = `${host}:${port}`;
      this.clients[z] = new MatcherClass(
        url,
        grpc.credentials.createInsecure(),
      ) as InterestMatcherGrpc;
      this.logger.log(`Interest Matcher Zone ${z} client → ${url}`);
    }
  }

  async onModuleDestroy() {
    // gRPC-js clients close automatically; nothing explicit needed
  }

  /**
   * Determine zone ID from entity longitude.
   */
  getZoneId(longitude: number): number {
    if (longitude < this.zone1Lng) return 0;
    if (longitude < this.zone2Lng) return 1;
    return 2;
  }

  /**
   * Publish a state update to the appropriate Interest Matcher broker.
   * Fire-and-forget (errors are logged, not thrown) to keep latency low.
   */
  async publish(
    entityLongitude: number,
    cellKey: string,
    payload: string,
  ): Promise<void> {
    const zoneId = this.getZoneId(entityLongitude);
    const client = this.clients[zoneId];

    if (!client) {
      this.logger.warn(`No Interest Matcher client for zone ${zoneId}`);
      return;
    }

    return new Promise((resolve) => {
      client.Publish(
        { cellKey, payload, entityLongitude },
        (err) => {
          if (err) {
            this.logger.error(
              `Publish to Zone ${zoneId} failed for cell ${cellKey}: ${err.message}`,
            );
          }
          resolve();
        },
      );
    });
  }
}
