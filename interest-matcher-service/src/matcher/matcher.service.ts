import {
  Injectable,
  Logger,
  OnModuleInit,
} from '@nestjs/common';
import { RedisService } from '../redis/redis.service';
import { ZoneService } from '../zone/zone.service';

/**
 * MatcherService — core logic of the Interest Matcher broker.
 *
 * Implements the SPS operations:
 *   PUBLISH   — receive a state update from state-computation-service
 *   SUBSCRIBE — register a dissemination node for a set of grid cells
 *   UNSUBSCRIBE — deregister
 *
 * Plus inter-broker forwarding:
 *   When an entity is at a border cell, its publication is forwarded to
 *   the neighboring broker's Redis Stream, where it is processed again
 *   and delivered to subscribers in that zone.
 *
 * Data structures (in-memory, per broker instance):
 *   cellSubscribers: Map<cellKey, Set<dissNodeId>>
 *     Which dissemination nodes are subscribed to each cell.
 *
 *   nodeCells: Map<dissNodeId, Set<cellKey>>
 *     Which cells each dissemination node is subscribed to (for cleanup).
 */
@Injectable()
export class MatcherService implements OnModuleInit {
  private readonly logger = new Logger(MatcherService.name);

  // cellKey → Set of dissemination node IDs subscribed
  private cellSubscribers = new Map<string, Set<string>>();
  // dissNodeId → Set of cellKeys (reverse index for cleanup)
  private nodeCells = new Map<string, Set<string>>();

  constructor(
    private readonly redisService: RedisService,
    private readonly zoneService: ZoneService,
  ) {}

  onModuleInit() {
    // Start consuming inter-broker forwarded messages from neighbor brokers
    void this.redisService.consumeInterBrokerStream(
      async (cellKey, payload) => {
        this.logger.debug(`Inter-broker message for cell ${cellKey}`);
        await this.deliverToSubscribers(cellKey, payload);
      },
    );

    this.logger.log(
      `MatcherService ready on Zone ${this.zoneService.getZoneId()}`,
    );
  }

  // ─── SPS PUBLISH ──────────────────────────────────────────────────────────

  /**
   * Handle a PUBLISH from state-computation-service.
   * 1. Deliver to all local subscribers of cellKey.
   * 2. If entity longitude is near a zone boundary → forward to neighbor broker.
   */
  async publish(
    cellKey: string,
    payload: string,
    entityLongitude: number,
  ): Promise<void> {
    // Deliver to local subscribers
    await this.deliverToSubscribers(cellKey, payload);

    // Inter-broker forwarding for border cells
    const targetZones = this.zoneService.getTargetZones(entityLongitude);
    const myZoneId = this.zoneService.getZoneId();

    for (const targetZoneId of targetZones) {
      if (targetZoneId !== myZoneId) {
        this.logger.debug(
          `Border cell ${cellKey} — forwarding to Zone ${targetZoneId}`,
        );
        await this.redisService.forwardToNeighborBroker(targetZoneId, cellKey, payload);
      }
    }
  }

  // ─── SPS SUBSCRIBE ────────────────────────────────────────────────────────

  /**
   * Register dissNodeId as interested in the given cell keys.
   * Called by state-dissemination-service when a client moves.
   */
  subscribe(dissNodeId: string, cellKeys: string[]): void {
    for (const cellKey of cellKeys) {
      if (!this.cellSubscribers.has(cellKey)) {
        this.cellSubscribers.set(cellKey, new Set());
      }
      this.cellSubscribers.get(cellKey)!.add(dissNodeId);
    }

    // Update reverse index
    if (!this.nodeCells.has(dissNodeId)) {
      this.nodeCells.set(dissNodeId, new Set());
    }
    const nodeSet = this.nodeCells.get(dissNodeId)!;
    cellKeys.forEach((k) => nodeSet.add(k));

    this.logger.debug(
      `SUBSCRIBE: node=${dissNodeId} cells=[${cellKeys.join(',')}]`,
    );
  }

  // ─── SPS UNSUBSCRIBE ──────────────────────────────────────────────────────

  /**
   * Deregister dissNodeId from specific cell keys.
   */
  unsubscribe(dissNodeId: string, cellKeys: string[]): void {
    for (const cellKey of cellKeys) {
      const subs = this.cellSubscribers.get(cellKey);
      if (subs) {
        subs.delete(dissNodeId);
        if (subs.size === 0) {
          this.cellSubscribers.delete(cellKey);
        }
      }
    }

    // Update reverse index
    const nodeSet = this.nodeCells.get(dissNodeId);
    if (nodeSet) {
      cellKeys.forEach((k) => nodeSet.delete(k));
      if (nodeSet.size === 0) {
        this.nodeCells.delete(dissNodeId);
      }
    }

    this.logger.debug(
      `UNSUBSCRIBE: node=${dissNodeId} cells=[${cellKeys.join(',')}]`,
    );
  }

  // ─── Internal Delivery ────────────────────────────────────────────────────

  /**
   * Push payload into the Redis Stream of each subscriber for cellKey.
   */
  private async deliverToSubscribers(cellKey: string, payload: string): Promise<void> {
    const subscribers = this.cellSubscribers.get(cellKey);
    if (!subscribers || subscribers.size === 0) return;

    const deliveryPromises: Promise<void>[] = [];
    for (const dissNodeId of subscribers) {
      deliveryPromises.push(
        this.redisService.deliverToDissNode(dissNodeId, payload),
      );
    }

    await Promise.all(deliveryPromises);
    this.logger.debug(
      `Delivered cell ${cellKey} to ${subscribers.size} subscriber(s)`,
    );
  }
}
