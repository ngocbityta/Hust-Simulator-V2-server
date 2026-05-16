import { Injectable, Logger } from '@nestjs/common';
import { WebSocket } from 'ws';
import { GrpcInterestMatcherClient } from '../grpc/interest-matcher.client';
import { IInterestService } from './interest.interface';
import { ISpatialService } from '../spatial/spatial.interface';
import { Inject } from '@nestjs/common';

/**
 * InterestService — manages AOI subscriptions for WebSocket clients.
 *
 * Architecture (multi-broker SPS):
 *   - Local maps track which WebSocket clients are interested in which cells
 *     (for fast message delivery within this dissemination node).
 *   - Interest Matcher gRPC calls register this node with the correct broker
 *     per zone, so the broker can push messages into our Redis Stream.
 *
 * The Interest Matcher broker handles:
 *   - Routing publications to subscribed dissemination nodes (via Redis Stream)
 *   - Inter-broker forwarding at zone boundaries
 *
 * This service handles:
 *   - Tracking which WebSocket clients need messages from which cells
 *   - Calling broker Subscribe/Unsubscribe when the set of needed cells changes
 *   - Reference counting to avoid redundant broker calls
 */
@Injectable()
export class InterestService implements IInterestService {
  private readonly logger = new Logger(InterestService.name);

  // cellKey → Set of WebSocket clients interested in that cell (local node only)
  private cellSubscriptions = new Map<string, Set<WebSocket>>();
  // WebSocket client → Set of cell keys it's currently subscribed to
  private clientInterests = new Map<WebSocket, Set<string>>();
  // cellKey → reference count (how many WS clients are interested)
  private nodeInterests = new Map<string, number>();

  constructor(
    private readonly interestMatcherClient: GrpcInterestMatcherClient,
    @Inject(ISpatialService) private readonly spatialService: ISpatialService,
  ) {}

  /**
   * Called whenever a client moves — updates local cell subscriptions and
   * calls broker Subscribe/Unsubscribe for the diff.
   *
   * @param client     WebSocket connection of the moving user
   * @param newAoiKeys Set of cell keys in the new AOI (9 cells)
   * @param aoiCells   Matching GridCell objects (needed for longitude lookup)
   */
  updateInterests(
    client: WebSocket,
    newAoiKeys: Set<string>,
    aoiCellLngMap?: Map<string, number>,
  ) {
    if (!newAoiKeys) {
      this.logger.error('updateInterests called with undefined newAoiKeys');
      return;
    }

    let currentInterests = this.clientInterests.get(client);
    if (!currentInterests) {
      currentInterests = new Set<string>();
    }

    // ── Keys to add ────────────────────────────────────────────────────────
    const keysToSubscribe: string[] = [];
    for (const key of newAoiKeys) {
      if (!currentInterests.has(key)) {
        const refCount = (this.nodeInterests.get(key) || 0) + 1;
        this.nodeInterests.set(key, refCount);

        if (refCount === 1) {
          // First client on this node interested in this cell → subscribe broker
          keysToSubscribe.push(key);
        }

        if (!this.cellSubscriptions.has(key)) {
          this.cellSubscriptions.set(key, new Set());
        }
        this.cellSubscriptions.get(key)!.add(client);
      }
    }

    // ── Keys to remove ─────────────────────────────────────────────────────
    const keysToUnsubscribe: string[] = [];
    for (const key of currentInterests) {
      if (!newAoiKeys.has(key)) {
        keysToUnsubscribe.push(key);
        this.removeLocalInterest(client, key);
      }
    }

    this.clientInterests.set(client, new Set(newAoiKeys));

    // ── Broker gRPC calls (async, non-blocking) ────────────────────────────
    if (keysToSubscribe.length > 0) {
      const lngMap = this.buildLngMap(keysToSubscribe, aoiCellLngMap);
      this.interestMatcherClient.subscribeCells(lngMap).catch((err) =>
        this.logger.error('Broker Subscribe error', err),
      );
    }
    if (keysToUnsubscribe.length > 0) {
      const lngMap = this.buildLngMap(keysToUnsubscribe, aoiCellLngMap);
      this.interestMatcherClient.unsubscribeCells(lngMap).catch((err) =>
        this.logger.error('Broker Unsubscribe error', err),
      );
    }
  }

  getClientsInCell(cellKey: string): Set<WebSocket> | undefined {
    return this.cellSubscriptions.get(cellKey);
  }

  removeClient(client: WebSocket) {
    const interests = this.clientInterests.get(client);
    if (!interests) return;

    const keysToUnsubscribe: string[] = [];
    interests.forEach((key) => {
      this.removeLocalInterest(client, key);
      // If ref count dropped to 0, unsubscribe from broker
      if (!this.nodeInterests.has(key)) {
        keysToUnsubscribe.push(key);
      }
    });
    this.clientInterests.delete(client);

    if (keysToUnsubscribe.length > 0) {
      const lngMap = this.buildLngMap(keysToUnsubscribe, undefined);
      this.interestMatcherClient.unsubscribeCells(lngMap).catch((err) =>
        this.logger.error('Broker Unsubscribe (disconnect) error', err),
      );
    }
  }

  // ─── Private helpers ───────────────────────────────────────────────────────

  private removeLocalInterest(client: WebSocket, key: string) {
    const clients = this.cellSubscriptions.get(key);
    if (clients) {
      clients.delete(client);
      if (clients.size === 0) {
        this.cellSubscriptions.delete(key);
        this.nodeInterests.delete(key);
      } else {
        this.nodeInterests.set(key, clients.size);
      }
    }
  }

  /**
   * Build a Map<cellKey, longitude> needed for zone routing in the broker client.
   * Falls back to extracting longitude from the cell key format "{x}:{y}".
   */
  private buildLngMap(
    keys: string[],
    provided?: Map<string, number>,
  ): Map<string, number> {
    const result = new Map<string, number>();
    for (const key of keys) {
      if (provided?.has(key)) {
        result.set(key, provided.get(key)!);
      } else {
        // Approximate longitude from cell x-coordinate
        // cellKey = "{x}:{y}", x = floor(lng * METERS_PER_LNG / cellSize)
        // Reverse: lng ≈ x * cellSize / METERS_PER_LNG
        const x = parseInt(key.split(':')[0], 10);
        const approxLng =
          (x * this.spatialService.getCellSize()) /
          this.spatialService.getMetersPerLng();
        result.set(key, approxLng);
      }
    }
    return result;
  }
}

