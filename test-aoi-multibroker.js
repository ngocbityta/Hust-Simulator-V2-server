#!/usr/bin/env node
/**
 * test-aoi-multibroker.js
 * End-to-end integration test for Multi-broker Interest Matcher AOI.
 *
 * Tests:
 *  1. Subscribe test-node to cells in different zones
 *  2. Publish a message to Zone 0 cell → verify delivery via Redis Stream
 *  3. Publish a message to Zone 1 cell → verify delivery via Redis Stream
 *  4. Publish a border-cell message → verify inter-broker forwarding
 *  5. Unsubscribe and verify no more messages
 *
 * Usage: node test-aoi-multibroker.js
 * Requires: @grpc/grpc-js @grpc/proto-loader ioredis
 */

const grpc = require('@grpc/grpc-js');
const protoLoader = require('@grpc/proto-loader');
const Redis = require('ioredis');
const path = require('path');

// ─── Config ────────────────────────────────────────────────────────────────
const REDIS_HOST = process.env.REDIS_HOST || '127.0.0.1';
const REDIS_PORT = parseInt(process.env.REDIS_PORT || '6379');
const REDIS_PASSWORD = process.env.REDIS_PASSWORD || 'hustsim_redis';

// Connect to broker containers via host network
// (docker-compose internal DNS not available from host, use localhost with mapped ports)
// We'll call into the brokers from INSIDE docker via exec, so use container names
const IM_HOSTS = [
  { host: process.env.IM_0_HOST || 'interest-matcher-0', port: 4000, zone: 0 },
  { host: process.env.IM_1_HOST || 'interest-matcher-1', port: 4001, zone: 1 },
  { host: process.env.IM_2_HOST || 'interest-matcher-2', port: 4002, zone: 2 },
];

const DISS_NODE_ID = 'test-node-001';
const GRID_CELL_SIZE = 50;
const METERS_PER_LNG = 111000 * Math.cos((21.003 * Math.PI) / 180);

// Zone boundaries
const ZONE_1_LNG = 105.84;
const ZONE_2_LNG = 105.85;

// ─── Helpers ───────────────────────────────────────────────────────────────
function getZoneId(lng) {
  if (lng < ZONE_1_LNG) return 0;
  if (lng < ZONE_2_LNG) return 1;
  return 2;
}

function lngToCellX(lng) {
  return Math.floor((lng * METERS_PER_LNG) / GRID_CELL_SIZE);
}

function latToCellY(lat) {
  return Math.floor((lat * 111000) / GRID_CELL_SIZE);
}

function cellKey(x, y) {
  return `${x}:${y}`;
}

// ─── gRPC client setup ─────────────────────────────────────────────────────
const PROTO_PATH = path.join(__dirname, '../proto/interest-matcher.proto');

const packageDef = protoLoader.loadSync(PROTO_PATH, {
  keepCase: false,
  longs: String,
  enums: String,
  defaults: true,
  oneofs: true,
});
const proto = grpc.loadPackageDefinition(packageDef);
const MatcherClass = proto.hustsimulator.interest_matcher.InterestMatcher;

const clients = IM_HOSTS.map(({ host, port }) =>
  new MatcherClass(`${host}:${port}`, grpc.credentials.createInsecure())
);

function grpcCall(client, method, request) {
  return new Promise((resolve, reject) => {
    client[method](request, (err, res) => {
      if (err) reject(err);
      else resolve(res);
    });
  });
}

// ─── Redis Stream consumer ─────────────────────────────────────────────────
async function consumeStreamOnce(redis, nodeId, timeoutMs = 3000) {
  const streamKey = `diss:${nodeId}:events`;
  const groupName = `test-${nodeId}-group`;
  const consumerName = `test-${nodeId}-consumer`;

  try {
    await redis.xgroup('CREATE', streamKey, groupName, '$', 'MKSTREAM');
  } catch {}

  const result = await redis.xreadgroup(
    'GROUP', groupName, consumerName,
    'COUNT', '10',
    'BLOCK', String(timeoutMs),
    'STREAMS', streamKey, '>'
  );

  if (!result) return [];
  const messages = [];
  for (const [, msgs] of result) {
    for (const [msgId, fields] of msgs) {
      const payloadIdx = fields.indexOf('payload');
      if (payloadIdx !== -1) messages.push(JSON.parse(fields[payloadIdx + 1]));
      await redis.xack(streamKey, groupName, msgId);
    }
  }
  return messages;
}

// ─── Test cases ────────────────────────────────────────────────────────────
let passed = 0;
let failed = 0;

async function assert(label, condition, detail = '') {
  if (condition) {
    console.log(`  ✅  ${label}`);
    passed++;
  } else {
    console.log(`  ❌  ${label}${detail ? ' — ' + detail : ''}`);
    failed++;
  }
}

// ─── Main ──────────────────────────────────────────────────────────────────
async function main() {
  const redis = new Redis({ host: REDIS_HOST, port: REDIS_PORT, password: REDIS_PASSWORD });

  // Clean up test stream from previous runs
  await redis.del(`diss:${DISS_NODE_ID}:events`).catch(() => {});

  console.log('\n══════════════════════════════════════════════════');
  console.log('  Multi-broker Interest Matcher — Integration Test');
  console.log('══════════════════════════════════════════════════\n');

  // ── Test 1: Zone 0 subscribe + publish ───────────────────────────────────
  console.log('【Test 1】 Zone 0 — Subscribe and receive a publication');
  const lng_z0 = 105.830;   // clearly Zone 0
  const lat_test = 21.003;
  const cx_z0 = lngToCellX(lng_z0);
  const cy_z0 = latToCellY(lat_test);
  const key_z0 = cellKey(cx_z0, cy_z0);

  try {
    const sub1 = await grpcCall(clients[0], 'Subscribe', {
      dissNodeId: DISS_NODE_ID,
      cellKeys: [key_z0],
    });
    await assert('Subscribe to Zone 0 cell succeeds', sub1.success, JSON.stringify(sub1));

    // Wait a moment, then publish
    await new Promise(r => setTimeout(r, 200));
    const pub1 = await grpcCall(clients[0], 'Publish', {
      cellKey: key_z0,
      payload: JSON.stringify({ userId: 'user-A', cellKey: key_z0, x: 1, y: 2 }),
      entityLongitude: lng_z0,
    });
    await assert('Publish to Zone 0 cell succeeds', pub1.success);

    const msgs1 = await consumeStreamOnce(redis, DISS_NODE_ID, 2000);
    await assert('Message delivered from Zone 0 broker', msgs1.length > 0, `Got ${msgs1.length} messages`);
    if (msgs1.length > 0) {
      await assert('Payload contains userId', msgs1[0].userId === 'user-A', JSON.stringify(msgs1[0]));
    }
  } catch (err) {
    console.log(`  ❌  Zone 0 test error: ${err.message}`);
    failed++;
  }

  // ── Test 2: Zone 1 subscribe + publish ───────────────────────────────────
  console.log('\n【Test 2】 Zone 1 — Subscribe and receive a publication');
  const lng_z1 = 105.845;   // Zone 1
  const cx_z1 = lngToCellX(lng_z1);
  const cy_z1 = latToCellY(lat_test);
  const key_z1 = cellKey(cx_z1, cy_z1);

  try {
    const sub2 = await grpcCall(clients[1], 'Subscribe', {
      dissNodeId: DISS_NODE_ID,
      cellKeys: [key_z1],
    });
    await assert('Subscribe to Zone 1 cell succeeds', sub2.success);

    await new Promise(r => setTimeout(r, 200));
    const pub2 = await grpcCall(clients[1], 'Publish', {
      cellKey: key_z1,
      payload: JSON.stringify({ userId: 'user-B', cellKey: key_z1, x: 10, y: 20 }),
      entityLongitude: lng_z1,
    });
    await assert('Publish to Zone 1 cell succeeds', pub2.success);

    const msgs2 = await consumeStreamOnce(redis, DISS_NODE_ID, 2000);
    await assert('Message delivered from Zone 1 broker', msgs2.length > 0, `Got ${msgs2.length} messages`);
    if (msgs2.length > 0) {
      await assert('Payload userId is user-B', msgs2[0].userId === 'user-B', JSON.stringify(msgs2[0]));
    }
  } catch (err) {
    console.log(`  ❌  Zone 1 test error: ${err.message}`);
    failed++;
  }

  // ─── Test 3: Border cell — inter-broker forwarding ─────────────────────
  console.log('\n【Test 3】 Border cell Zone 0→1 — inter-broker forwarding');
  // Must be within borderMarginDeg (= CELL_SIZE/METERS_PER_LNG ≈ 0.000483°) of zone1Lng=105.840
  // Using 105.8396 → distToZone1 = 0.0004° < 0.000483° → border detected ✓
  const lng_border = 105.8396;
  const cx_border = lngToCellX(lng_border);
  const cy_border = latToCellY(lat_test);
  const key_border = cellKey(cx_border, cy_border);

  try {
    // Subscribe at Zone 1 broker for this border cell
    await grpcCall(clients[1], 'Subscribe', {
      dissNodeId: DISS_NODE_ID,
      cellKeys: [key_border],
    });

    await new Promise(r => setTimeout(r, 200));

    // Publish from Zone 0 broker (entity is in zone 0 but near border)
    const pub3 = await grpcCall(clients[0], 'Publish', {
      cellKey: key_border,
      payload: JSON.stringify({ userId: 'user-C', cellKey: key_border, type: 'border-test' }),
      entityLongitude: lng_border,
    });
    await assert('Publish border-cell via Zone 0 broker succeeds', pub3.success);

    // Wait a bit longer for inter-broker forwarding
    const msgs3 = await consumeStreamOnce(redis, DISS_NODE_ID, 3000);
    await assert(
      'Inter-broker forwarding: Zone 1 subscriber received Zone 0 border publication',
      msgs3.length > 0,
      `Got ${msgs3.length} messages (expected ≥1 via inter-broker forward)`
    );
  } catch (err) {
    console.log(`  ❌  Border cell test error: ${err.message}`);
    failed++;
  }

  // ── Test 4: Unsubscribe ───────────────────────────────────────────────────
  console.log('\n【Test 4】 Unsubscribe — no more messages after unsubscribe');
  try {
    await grpcCall(clients[0], 'Unsubscribe', {
      dissNodeId: DISS_NODE_ID,
      cellKeys: [key_z0],
    });

    await new Promise(r => setTimeout(r, 200));

    // Publish again to the same cell
    await grpcCall(clients[0], 'Publish', {
      cellKey: key_z0,
      payload: JSON.stringify({ userId: 'user-D', cellKey: key_z0 }),
      entityLongitude: lng_z0,
    });

    const msgs4 = await consumeStreamOnce(redis, DISS_NODE_ID, 1500);
    await assert('No message received after unsubscribe', msgs4.length === 0, `Got ${msgs4.length} messages`);
  } catch (err) {
    console.log(`  ❌  Unsubscribe test error: ${err.message}`);
    failed++;
  }

  // ── Test 5: Dissemination service logs ────────────────────────────────────
  console.log('\n【Test 5】 Check service connectivity logs');
  // Just validate we can ping Redis
  const ping = await redis.ping();
  await assert('Redis is reachable', ping === 'PONG');

  // ─── Summary ───────────────────────────────────────────────────────────────
  await redis.quit();
  console.log('\n══════════════════════════════════════════════════');
  console.log(`  Result: ${passed} passed, ${failed} failed`);
  console.log('══════════════════════════════════════════════════\n');
  process.exit(failed > 0 ? 1 : 0);
}

main().catch(err => {
  console.error('Test runner error:', err);
  process.exit(1);
});
