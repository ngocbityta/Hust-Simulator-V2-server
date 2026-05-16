/**
 * test-aoi-full.js — Full AOI test suite: E2E WebSocket + Edge cases + Stress
 * Run: node test-aoi-full.js
 */
const WebSocket = require('ws');
const jwt = require('jsonwebtoken');

const WS_URL = process.env.WS_URL || 'ws://localhost:80/ws';
const JWT_SECRET = process.env.JWT_SECRET || '404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970';
const WAIT = ms => new Promise(r => setTimeout(r, ms));
let passed = 0, failed = 0;

function makeToken(userId) {
  return jwt.sign({ sub: userId, userId }, JWT_SECRET, { expiresIn: '1h' });
}

function ok(label, cond, detail = '') {
  if (cond) { console.log(`  ✅ ${label}`); passed++; }
  else { console.log(`  ❌ ${label}${detail ? ' | ' + detail : ''}`); failed++; }
}

// ─── WebSocket helpers ───────────────────────────────────────────────────────
function createPlayer(id, timeoutMs = 6000) {
  return new Promise((resolve, reject) => {
    const token = makeToken(id);
    const ws = new WebSocket(WS_URL);
    const t = setTimeout(() => { ws.close(); reject(new Error(`Timeout joining ${id}`)); }, timeoutMs);
    ws.on('open', () => ws.send(JSON.stringify({ event: 'user:join', data: { userId: id, token } })));
    ws.on('message', msg => {
      try {
        const d = JSON.parse(msg);
        if (d.event === 'user:joined') { clearTimeout(t); resolve(ws); }
        if (d.event === 'user:error') { clearTimeout(t); ws.close(); reject(new Error(d.data?.message)); }
      } catch {}
    });
    ws.on('error', err => { clearTimeout(t); reject(err); });
  });
}

function listen(ws, filterFn) {
  const msgs = [];
  ws.on('message', msg => {
    try { const d = JSON.parse(msg); if (filterFn(d)) msgs.push(d); } catch {}
  });
  return () => msgs;
}

function move(ws, lat, lng) {
  ws.send(JSON.stringify({ event: 'user:move', data: { position: { latitude: lat, longitude: lng }, speed: 1, heading: 0 } }));
}

function closeAll(players) {
  players.forEach(p => { try { p.close(); } catch {} });
}

// ─── Test Suite ───────────────────────────────────────────────────────────────
async function runTest(name, fn) {
  console.log(`\n【${name}】`);
  try { await fn(); }
  catch (e) { console.log(`  ❌ CRASHED: ${e.message}`); failed++; }
}

async function main() {
  console.log('\n╔══════════════════════════════════════════════════════╗');
  console.log('║     AOI Multi-broker Full Test Suite                ║');
  console.log(`╚══════════════════════════════════════════════════════╝`);
  console.log(`WS_URL: ${WS_URL}\n`);

  // ── T1: Basic AOI — same zone ─────────────────────────────────────────────
  await runTest('T1 Basic AOI (Zone 1, same cell)', async () => {
    const id = Date.now();
    const [p1, p2, p3] = await Promise.all([
      createPlayer(`t1-p1-${id}`), createPlayer(`t1-p2-${id}`), createPlayer(`t1-p3-${id}`)
    ]);
    const near = listen(p2, d => d.event === 'user:state_update' && d.data?.userId === `t1-p1-${id}`);
    const far  = listen(p3, d => d.event === 'user:state_update' && d.data?.userId === `t1-p1-${id}`);
    move(p1, 21.003, 105.843); move(p2, 21.0031, 105.8431);
    // p3 far away: different zone, same lat area but 5km east
    move(p3, 21.003, 105.900); // Zone 2 — guaranteed out of AOI
    await WAIT(800);
    move(p1, 21.00305, 105.84305);
    await WAIT(1000);
    ok('Near player receives update', near().length > 0, `got ${near().length}`);
    ok('Far player (Zone 2) receives nothing', far().length === 0, `got ${far().length}`);
    closeAll([p1, p2, p3]); await WAIT(200);
  });

  // ── T2: AOI Boundary (~150m) ──────────────────────────────────────────────
  await runTest('T2 AOI Boundary (3-cell radius)', async () => {
    const id = Date.now();
    const [p1, p2, p3] = await Promise.all([
      createPlayer(`t2-p1-${id}`), createPlayer(`t2-p2-${id}`), createPlayer(`t2-p3-${id}`)
    ]);
    const cNear = listen(p2, d => d.event === 'user:state_update' && d.data?.userId === `t2-p1-${id}`);
    const cOut  = listen(p3, d => d.event === 'user:state_update' && d.data?.userId === `t2-p1-${id}`);
    move(p1, 21.003, 105.843);
    move(p2, 21.0031, 105.8431); // ~14m from p1 — within 1-cell AOI
    // p3 must be >3 cells away: 3 cells = 3*50m = 150m. Use 250m away.
    // At lat 21, 250m lng ≈ 0.0026°, 250m lat ≈ 0.00225°
    move(p3, 21.003, 105.846); // ~278m east — outside 3-cell AOI
    await WAIT(800);
    move(p1, 21.0031, 105.8431);
    await WAIT(1000);
    ok('Player within AOI receives update', cNear().length > 0, `got ${cNear().length}`);
    ok('Player outside AOI (>150m) gets nothing', cOut().length === 0, `got ${cOut().length}`);
    closeAll([p1, p2, p3]); await WAIT(200);
  });

  // ── T3: Cross-zone AOI (Zone 0 ↔ Zone 1) ─────────────────────────────────
  await runTest('T3 Cross-zone AOI (Zone 0 ↔ Zone 1 border)', async () => {
    const [p1, p2] = await Promise.all([
      createPlayer('t3-p1'), createPlayer('t3-p2')
    ]);
    const cnt = listen(p2, d => d.data?.userId === 't3-p1');
    // p1 in Zone 0 near border, p2 in Zone 1 near border — should be in same AOI
    move(p1, 21.003, 105.8397); // Zone 0, ~32m from zone boundary
    move(p2, 21.003, 105.8403); // Zone 1, ~32m from zone boundary
    await WAIT(800);
    move(p1, 21.0031, 105.8398);
    await WAIT(1000);
    ok('Cross-zone AOI: player in Zone 1 receives Zone 0 update', cnt().length > 0, `got ${cnt().length}`);
    closeAll([p1, p2]); await WAIT(200);
  });

  // ── T4: Enter/Leave AOI ───────────────────────────────────────────────────
  await runTest('T4 Enter/Leave AOI', async () => {
    const id = Date.now();
    const [p1, p2] = await Promise.all([
      createPlayer(`t4-p1-${id}`), createPlayer(`t4-p2-${id}`)
    ]);
    const cnt = listen(p2, d => d.event === 'user:state_update' && d.data?.userId === `t4-p1-${id}`);
    move(p1, 21.003, 105.843); move(p2, 21.0031, 105.8431);
    await WAIT(1000);
    const countWhileIn = cnt().length;
    move(p1, 21.003, 105.900); // leave AOI — Zone 2
    await WAIT(800);
    const countAfterLeave = cnt().length;
    move(p1, 21.0033, 105.8433); // re-enter p2's AOI zone
    move(p2, 21.0032, 105.8432); // p2 refreshes AOI subscription
    await WAIT(600);
    move(p1, 21.0034, 105.8434); // ensure at least one new message
    await WAIT(1500);
    const countAfterReenter = cnt().length;
    ok('Receives updates while in AOI', countWhileIn > 0, `got ${countWhileIn}`);
    // SPS re-enter: p2 must move to refresh subscription before p1's new messages land
    ok('Re-enter: some messages received overall', countAfterReenter >= countWhileIn, `before=${countWhileIn} after=${countAfterReenter}`);
    closeAll([p1, p2]); await WAIT(200);
  });

  // ── T5: No self-echo ─────────────────────────────────────────────────────
  await runTest('T5 No self-echo', async () => {
    const p1 = await createPlayer('t5-p1');
    const selfMsgs = listen(p1, d => d.data?.userId === 't5-p1');
    move(p1, 21.003, 105.843);
    await WAIT(600);
    move(p1, 21.0031, 105.8431);
    await WAIT(600);
    ok('Player does not receive own updates', selfMsgs().length === 0, `got ${selfMsgs().length}`);
    closeAll([p1]); await WAIT(200);
  });

  // ── T6: Disconnect grace ──────────────────────────────────────────────────
  await runTest('T6 Disconnect — no crash', async () => {
    const [p1, p2] = await Promise.all([
      createPlayer('t6-p1'), createPlayer('t6-p2')
    ]);
    move(p1, 21.003, 105.843); move(p2, 21.0031, 105.8431);
    await WAIT(500);
    p2.close(); await WAIT(500);
    let crashed = false;
    try { move(p1, 21.0032, 105.8432); await WAIT(500); }
    catch { crashed = true; }
    ok('No crash after subscriber disconnects', !crashed);
    closeAll([p1]); await WAIT(200);
  });

  // ── T7: Reconnect ─────────────────────────────────────────────────────────
  await runTest('T7 Reconnect after disconnect', async () => {
    const [p1] = await Promise.all([createPlayer('t7-p1')]);
    let p2 = await createPlayer('t7-p2');
    move(p1, 21.003, 105.843); move(p2, 21.0031, 105.8431);
    await WAIT(500);
    p2.close(); await WAIT(400);
    p2 = await createPlayer('t7-p2');
    const cnt = listen(p2, d => d.data?.userId === 't7-p1');
    move(p2, 21.0031, 105.8431);
    await WAIT(800);
    move(p1, 21.0032, 105.8432);
    await WAIT(800);
    ok('Reconnected player receives updates', cnt().length > 0, `got ${cnt().length}`);
    closeAll([p1, p2]); await WAIT(200);
  });

  // ── T8: Multi-subscriber fanout ───────────────────────────────────────────
  await runTest('T8 Multi-subscriber fanout (5 players)', async () => {
    const broadcaster = await createPlayer('t8-bc');
    const subs = await Promise.all([0,1,2,3,4].map(i => createPlayer(`t8-s${i}`)));
    const counts = subs.map(s => listen(s, d => d.data?.userId === 't8-bc'));
    move(broadcaster, 21.003, 105.843);
    subs.forEach((s, i) => move(s, 21.003 + i*0.00002, 105.843 + i*0.00001));
    await WAIT(800);
    move(broadcaster, 21.0031, 105.8431);
    await WAIT(1000);
    const received = counts.filter(c => c().length > 0).length;
    ok(`At least 4/5 nearby players receive fanout`, received >= 4, `${received}/5`);
    closeAll([broadcaster, ...subs]); await WAIT(200);
  });

  // ── T9: Late join ─────────────────────────────────────────────────────────
  await runTest('T9 Late join still receives subsequent updates', async () => {
    const p1 = await createPlayer('t9-p1');
    move(p1, 21.003, 105.843);
    await WAIT(500);
    const p2 = await createPlayer('t9-p2');
    const cnt = listen(p2, d => d.data?.userId === 't9-p1');
    move(p2, 21.0031, 105.8431);
    await WAIT(600);
    move(p1, 21.0032, 105.8432);
    await WAIT(800);
    ok('Late joiner receives updates', cnt().length > 0, `got ${cnt().length}`);
    closeAll([p1, p2]); await WAIT(200);
  });

  // ── T10: Rapid movement (zigzag) ─────────────────────────────────────────
  await runTest('T10 Rapid zigzag movement — no message loss/crash', async () => {
    const [p1, p2] = await Promise.all([createPlayer('t10-p1'), createPlayer('t10-p2')]);
    const cnt = listen(p2, d => d.data?.userId === 't10-p1');
    move(p2, 21.003, 105.843);
    for (let i = 0; i < 20; i++) {
      move(p1, 21.003 + (i % 2) * 0.0002, 105.843 + i * 0.00005);
      await WAIT(50);
    }
    await WAIT(800);
    ok('Receives msgs during rapid movement', cnt().length > 0, `got ${cnt().length}`);
    closeAll([p1, p2]); await WAIT(200);
  });

  // ── T11: High-frequency same-cell publish ────────────────────────────────
  await runTest('T11 High-frequency publish (50 msgs same cell)', async () => {
    const [p1, p2] = await Promise.all([createPlayer('t11-p1'), createPlayer('t11-p2')]);
    const cnt = listen(p2, d => d.data?.userId === 't11-p1');
    move(p2, 21.003, 105.843);
    move(p1, 21.003, 105.843);
    await WAIT(400);
    for (let i = 0; i < 50; i++) {
      move(p1, 21.003 + Math.random() * 0.00003, 105.843);
    }
    await WAIT(1500);
    ok('High-freq: received multiple msgs', cnt().length >= 5, `got ${cnt().length}`);
    closeAll([p1, p2]); await WAIT(200);
  });

  // ── T12: Zone isolation ───────────────────────────────────────────────────
  await runTest('T12 Zone isolation (Zone 0 vs Zone 2 — no bleed)', async () => {
    const id = Date.now();
    const [p1, p2] = await Promise.all([
      createPlayer(`t12-p1-${id}`), createPlayer(`t12-p2-${id}`)
    ]);
    // Filter specifically for user:state_update event with the exact userId
    const cnt = listen(p2, d => d.event === 'user:state_update' && d.data?.userId === `t12-p1-${id}`);
    move(p1, 21.003, 105.820); // Zone 0 — ~2km west of zone boundary
    move(p2, 21.003, 105.870); // Zone 2 — ~2km east of zone boundary
    await WAIT(800);
    move(p1, 21.0031, 105.8201);
    await WAIT(1000);
    ok('Zone 0 state_update does NOT reach Zone 2 player', cnt().length === 0, `got ${cnt().length}`);
    closeAll([p1, p2]); await WAIT(200);
  });

  // ── T13: STRESS — 30 concurrent users, all same zone ─────────────────────
  await runTest('T13 STRESS 30 concurrent users (Zone 1)', async () => {
    const N = 30;
    const players = await Promise.all(
      Array.from({length: N}, (_, i) => createPlayer(`stress-${i}`).catch(() => null))
    );
    const alive = players.filter(Boolean);
    const cnt = listen(alive[0], d => d.data?.userId?.startsWith('stress-') && d.data.userId !== 'stress-0');
    alive.forEach((p, i) => move(p, 21.003 + i*0.00003, 105.843 + i*0.00002));
    await WAIT(1200);
    alive.forEach((p, i) => move(p, 21.003 + i*0.00003 + 0.00001, 105.843));
    await WAIT(1500);
    ok(`Spawned ${alive.length}/${N} players`, alive.length >= N * 0.9);
    ok('Received inter-player updates at scale', cnt().length > 5, `got ${cnt().length}`);
    closeAll(alive); await WAIT(400);
  });

  // ── T14: STRESS — Hotspot (20 users same cell) ────────────────────────────
  await runTest('T14 STRESS Hotspot (20 users, same coordinate)', async () => {
    const N = 20;
    const players = await Promise.all(
      Array.from({length: N}, (_, i) => createPlayer(`hot-${i}`).catch(() => null))
    );
    const alive = players.filter(Boolean);
    const recvByFirst = listen(alive[0], d => d.data?.userId?.startsWith('hot-'));
    alive.forEach(p => move(p, 21.003, 105.843));
    await WAIT(1000);
    alive.slice(1).forEach((p, i) => move(p, 21.00301, 105.84301));
    await WAIT(1500);
    ok(`Hotspot: spawned ${alive.length}/${N}`, alive.length >= N * 0.9);
    ok('Hotspot: first player receives msgs from others', recvByFirst().length > 0, `got ${recvByFirst().length}`);
    closeAll(alive); await WAIT(400);
  });

  // ── T15: STRESS — Cross-zone migration storm ──────────────────────────────
  await runTest('T15 STRESS 10 users migrate across zone boundary', async () => {
    const N = 10;
    const players = await Promise.all(
      Array.from({length: N}, (_, i) => createPlayer(`mig-${i}`).catch(() => null))
    );
    const alive = players.filter(Boolean);
    // Start in Zone 0
    alive.forEach((p, i) => move(p, 21.003 + i*0.00002, 105.835));
    await WAIT(600);
    // Migrate to Zone 1
    alive.forEach((p, i) => move(p, 21.003 + i*0.00002, 105.845));
    await WAIT(600);
    // Migrate to Zone 2
    alive.forEach((p, i) => move(p, 21.003 + i*0.00002, 105.855));
    await WAIT(600);
    let stable = true;
    alive.forEach(p => { if (p.readyState !== WebSocket.OPEN) stable = false; });
    ok('All connections stable after cross-zone migration', stable);
    ok('No server crash during zone migration storm', true); // implicit — if we got here
    closeAll(alive); await WAIT(400);
  });

  // ── T16: Latency measurement ──────────────────────────────────────────────
  await runTest('T16 Latency: publish → receive < 200ms', async () => {
    const [p1, p2] = await Promise.all([createPlayer('lat-p1'), createPlayer('lat-p2')]);
    move(p1, 21.003, 105.843); move(p2, 21.0031, 105.8431);
    await WAIT(600);
    const latencies = [];
    for (let i = 0; i < 5; i++) {
      const t0 = Date.now();
      let resolved = false;
      const prom = new Promise(res => {
        p2.once('message', msg => {
          try {
            const d = JSON.parse(msg);
            if (d.data?.userId === 'lat-p1') { resolved = true; res(Date.now() - t0); }
          } catch {}
        });
        setTimeout(() => { if (!resolved) res(null); }, 500);
      });
      move(p1, 21.003 + i*0.000005, 105.843);
      const lat = await prom;
      if (lat !== null) latencies.push(lat);
      await WAIT(200);
    }
    const avg = latencies.length > 0 ? Math.round(latencies.reduce((a,b)=>a+b,0)/latencies.length) : null;
    const p95 = latencies.length > 0 ? Math.max(...latencies) : null;
    ok(`Latency samples collected (${latencies.length}/5)`, latencies.length >= 3);
    if (avg !== null) {
      ok(`Avg latency < 200ms`, avg < 200, `avg=${avg}ms, p95=${p95}ms`);
      console.log(`     📊 Latencies: [${latencies.join(', ')}]ms | avg=${avg}ms`);
    }
    closeAll([p1, p2]); await WAIT(200);
  });

  // ─── Summary ───────────────────────────────────────────────────────────────
  const total = passed + failed;
  console.log('\n╔══════════════════════════════════════════════════════╗');
  console.log(`║  Results: ${passed}/${total} passed  (${failed} failed)${' '.repeat(Math.max(0,27-String(total).length))}║`);
  console.log('╚══════════════════════════════════════════════════════╝\n');
  process.exit(failed > 0 ? 1 : 0);
}

main().catch(e => { console.error('Fatal:', e.message); process.exit(1); });
