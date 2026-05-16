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
  Subscribe(
    req: { dissNodeId: string; cellKeys: string[] },
    cb: (err: any, res: { success: boolean }) => void,
  ): void;
  Unsubscribe(
    req: { dissNodeId: string; cellKeys: string[] },
    cb: (err: any, res: { success: boolean }) => void,
  ): void;
}

/**
 * GrpcInterestMatcherClient (state-dissemination-service side).
 *
 * Called by InterestService to register/deregister AOI cell subscriptions
 * with the appropriate Interest Matcher broker (zone-based routing).
 *
 * Each broker manages one spatial zone. When a client's AOI spans multiple
 * zones, this client calls Subscribe on each relevant broker.
 */
@Injectable()
export class GrpcInterestMatcherClient implements OnModuleInit, OnModuleDestroy {
  private readonly logger = new Logger(GrpcInterestMatcherClient.name);

  private clients: InterestMatcherGrpc[] = [];
  private readonly zone1Lng: number;
  private readonly zone2Lng: number;
  private readonly numZones: number;
  readonly dissNodeId: string;

  constructor(private readonly configService: ConfigService) {
    this.zone1Lng = parseFloat(String(this.configService.get('ZONE_1_LNG', '105.84')));
    this.zone2Lng = parseFloat(String(this.configService.get('ZONE_2_LNG', '105.85')));
    this.numZones = parseInt(
      String(this.configService.get('IM_NUM_ZONES', '3')),
      10,
    );
    // NODE_ID uniquely identifies this dissemination instance in the broker
    this.dissNodeId = this.configService.get<string>(
      'DISS_NODE_ID',
      process.env.HOSTNAME ?? 'diss-default',
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
      const url = `${host}:${basePort + z}`;
      this.clients[z] = new MatcherClass(
        url,
        grpc.credentials.createInsecure(),
      ) as InterestMatcherGrpc;
      this.logger.log(`Interest Matcher Zone ${z} client → ${url} (nodeId: ${this.dissNodeId})`);
    }
  }

  async onModuleDestroy() {
    // gRPC-js handles connection cleanup automatically
  }

  getZoneId(longitude: number): number {
    if (longitude < this.zone1Lng) return 0;
    if (longitude < this.zone2Lng) return 1;
    return 2;
  }

  /**
   * Subscribe to cell keys, grouped by zone → one gRPC call per broker.
   * cellKeyLng: Map<cellKey, longitude> needed for zone routing.
   */
  async subscribeCells(cellKeyLngMap: Map<string, number>): Promise<void> {
    const byZone = new Map<number, string[]>();
    for (const [cellKey, lng] of cellKeyLngMap) {
      const z = this.getZoneId(lng);
      if (!byZone.has(z)) byZone.set(z, []);
      byZone.get(z)!.push(cellKey);
    }

    const calls: Promise<void>[] = [];
    for (const [zoneId, keys] of byZone) {
      calls.push(this.grpcSubscribe(zoneId, keys));
    }
    await Promise.all(calls);
  }

  /**
   * Unsubscribe from cell keys, grouped by zone.
   */
  async unsubscribeCells(cellKeyLngMap: Map<string, number>): Promise<void> {
    const byZone = new Map<number, string[]>();
    for (const [cellKey, lng] of cellKeyLngMap) {
      const z = this.getZoneId(lng);
      if (!byZone.has(z)) byZone.set(z, []);
      byZone.get(z)!.push(cellKey);
    }

    const calls: Promise<void>[] = [];
    for (const [zoneId, keys] of byZone) {
      calls.push(this.grpcUnsubscribe(zoneId, keys));
    }
    await Promise.all(calls);
  }

  private grpcSubscribe(zoneId: number, cellKeys: string[]): Promise<void> {
    const client = this.clients[zoneId];
    if (!client) return Promise.resolve();
    return new Promise((resolve) => {
      client.Subscribe({ dissNodeId: this.dissNodeId, cellKeys }, (err) => {
        if (err) {
          this.logger.error(`Subscribe Zone ${zoneId} failed: ${err.message}`);
        }
        resolve();
      });
    });
  }

  private grpcUnsubscribe(zoneId: number, cellKeys: string[]): Promise<void> {
    const client = this.clients[zoneId];
    if (!client) return Promise.resolve();
    return new Promise((resolve) => {
      client.Unsubscribe({ dissNodeId: this.dissNodeId, cellKeys }, (err) => {
        if (err) {
          this.logger.error(`Unsubscribe Zone ${zoneId} failed: ${err.message}`);
        }
        resolve();
      });
    });
  }
}
