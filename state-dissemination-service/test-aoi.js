const WebSocket = require('ws');

const WS_URL = 'ws://localhost:3002/ws';
const WAIT = (ms) => new Promise(r => setTimeout(r, ms));

/* ================= HELPERS ================= */

function createPlayer(id) {
  const ws = new WebSocket(WS_URL);

  return new Promise(resolve => {
    ws.on('open', () => {
      ws.send(JSON.stringify({
        event: 'user:join',
        data: { userId: id }
      }));
    });

    ws.on('message', (msg) => {
      try {
        const data = JSON.parse(msg);
        if (data.event === 'user:joined') {
          resolve(ws);
        }
      } catch { }
    });
  });
}

function listen(ws, filterFn) {
  let count = 0;
  ws.on('message', (msg) => {
    try {
      const data = JSON.parse(msg);
      if (filterFn(data)) count++;
    } catch { }
  });
  return () => count;
}

function move(ws, lat, lng) {
  ws.send(JSON.stringify({
    event: 'user:move',
    data: {
      position: { latitude: lat, longitude: lng },
      speed: 1,
      heading: 0
    }
  }));
}

async function cleanup(players) {
  players.forEach(p => p.close());
  await WAIT(300);
}

/* ================= TESTS ================= */

async function test1_basicAOI() {
  console.log('Test 1: Basic AOI');

  const p1 = await createPlayer('t1-p1');
  const p2 = await createPlayer('t1-p2');
  const p3 = await createPlayer('t1-p3');

  const countP2 = listen(p2, d => d.data?.userId === 't1-p1');
  const countP3 = listen(p3, d => d.data?.userId === 't1-p1');

  move(p1, 21.003, 105.843);
  move(p2, 21.0031, 105.8431);
  move(p3, 21.1, 105.9);

  await WAIT(1000);

  move(p1, 21.00305, 105.84305);
  await WAIT(1000);

  console.log(countP2(), countP3());
  await cleanup([p1, p2, p3]);
}

async function test2_boundary() {
  console.log('Test 2: AOI Boundary');

  const p1 = await createPlayer('t2-p1');
  const p2 = await createPlayer('t2-p2');

  const count = listen(p2, d => d.data?.userId === 't2-p1');

  move(p1, 21.003, 105.843);
  move(p2, 21.0034, 105.8434); // ~50m boundary

  await WAIT(1000);
  move(p1, 21.0031, 105.8431);
  await WAIT(1000);

  console.log('Received:', count());
  await cleanup([p1, p2]);
}

async function test3_enterLeave() {
  console.log('Test 3: Enter/Leave AOI');

  const p1 = await createPlayer('t3-p1');
  const p2 = await createPlayer('t3-p2');

  const count = listen(p2, d => d.data?.userId === 't3-p1');

  move(p1, 21.003, 105.843);
  move(p2, 21.0031, 105.8431);
  await WAIT(1000);

  move(p1, 21.1, 105.9); // move far
  await WAIT(1000);

  console.log('Received:', count());
  await cleanup([p1, p2]);
}

async function test4_crossCell() {
  console.log('Test 4: Cross Cell');

  const p1 = await createPlayer('t4-p1');
  const p2 = await createPlayer('t4-p2');

  const count = listen(p2, d => d.data?.userId === 't4-p1');

  move(p1, 21.003, 105.843);
  move(p2, 21.0031, 105.8431);

  await WAIT(1000);

  move(p1, 21.004, 105.844); // different cell
  await WAIT(1000);

  console.log('Received:', count());
  await cleanup([p1, p2]);
}

async function test5_zigzag() {
  console.log('Test 5: Zigzag');

  const p1 = await createPlayer('t5-p1');
  const p2 = await createPlayer('t5-p2');

  const count = listen(p2, d => d.data?.userId === 't5-p1');

  move(p2, 21.003, 105.843);

  for (let i = 0; i < 10; i++) {
    move(p1, 21.003 + i * 0.0001, 105.843 + i * 0.0001);
    await WAIT(100);
  }

  console.log('Received:', count());
  await cleanup([p1, p2]);
}

async function test6_highFreq() {
  console.log('Test 6: High Frequency');

  const p1 = await createPlayer('t6-p1');
  const p2 = await createPlayer('t6-p2');

  const count = listen(p2, d => d.data?.userId === 't6-p1');

  move(p2, 21.003, 105.843);

  for (let i = 0; i < 50; i++) {
    move(p1, 21.003 + Math.random() * 0.0005, 105.843);
  }

  await WAIT(1000);
  console.log('Received:', count());

  await cleanup([p1, p2]);
}

async function test7_channelIsolation() {
  console.log('Test 7: Channel Isolation');

  const p1 = await createPlayer('t7-p1');
  const p2 = await createPlayer('t7-p2');

  const count = listen(p2, d => d.data?.userId === 't7-p1');

  move(p1, 21.003, 105.843);
  move(p2, 21.1, 105.9);

  await WAIT(1000);
  move(p1, 21.0031, 105.8431);

  await WAIT(1000);
  console.log('Received:', count());

  await cleanup([p1, p2]);
}

async function test8_multiSubscriber() {
  console.log('Test 8: Multi Subscriber');

  const p1 = await createPlayer('t8-p1');
  const p2 = await createPlayer('t8-p2');
  const p3 = await createPlayer('t8-p3');

  const c2 = listen(p2, d => d.data?.userId === 't8-p1');
  const c3 = listen(p3, d => d.data?.userId === 't8-p1');

  move(p1, 21.003, 105.843);
  move(p2, 21.0031, 105.8431);
  move(p3, 21.0032, 105.8432);

  await WAIT(1000);
  move(p1, 21.0033, 105.8433);

  await WAIT(1000);
  console.log(c2(), c3());

  await cleanup([p1, p2, p3]);
}

async function test9_lateJoin() {
  console.log('Test 9: Late Join');

  const p1 = await createPlayer('t9-p1');

  move(p1, 21.003, 105.843);
  await WAIT(500);

  const p2 = await createPlayer('t9-p2');
  const count = listen(p2, d => d.data?.userId === 't9-p1');

  move(p2, 21.0031, 105.8431);
  await WAIT(1000);

  move(p1, 21.0032, 105.8432);
  await WAIT(1000);

  console.log('Received:', count());
  await cleanup([p1, p2]);
}

async function test10_disconnect() {
  console.log('Test 10: Disconnect');

  const p1 = await createPlayer('t10-p1');
  const p2 = await createPlayer('t10-p2');

  move(p1, 21.003, 105.843);
  move(p2, 21.0031, 105.8431);

  await WAIT(500);
  p2.close();

  await WAIT(1000);
  move(p1, 21.0032, 105.8432);

  await WAIT(500);
  console.log('No crash');

  await cleanup([p1]);
}

async function test11_reconnect() {
  console.log('Test 11: Reconnect');

  const p1 = await createPlayer('t11-p1');
  let p2 = await createPlayer('t11-p2');

  move(p1, 21.003, 105.843);
  move(p2, 21.0031, 105.8431);

  await WAIT(500);
  p2.close();

  await WAIT(500);
  p2 = await createPlayer('t11-p2');

  move(p2, 21.0031, 105.8431);
  await WAIT(1000);

  console.log('Reconnect success');
  await cleanup([p1, p2]);
}

async function test12_redisRestartNote() {
  console.log('Test 12: Redis restart (manual)');
  console.log('👉 Restart Redis manually and observe logs');
}

async function test13_100players() {
  console.log('Test 13: 100 players');

  const players = [];
  for (let i = 0; i < 50; i++) {
    players.push(await createPlayer(`t13-${i}`));
  }

  players.forEach((p, i) => {
    move(p, 21.003 + i * 0.00001, 105.843);
  });

  await WAIT(2000);
  console.log('Spawned 50 players');

  await cleanup(players);
}

async function test14_hotspot() {
  console.log('Test 14: Hotspot');

  const players = [];
  for (let i = 0; i < 20; i++) {
    players.push(await createPlayer(`t14-${i}`));
  }

  players.forEach(p => move(p, 21.003, 105.843));

  await WAIT(2000);
  console.log('Hotspot test done');

  await cleanup(players);
}

async function test15_multiNodeNote() {
  console.log('Test 15: Multi-node (manual)');
  console.log('👉 Run multiple instances and verify cross-node pub/sub');
}

/* ================= RUNNER ================= */

async function run() {
  await test1_basicAOI();
  await test2_boundary();
  await test3_enterLeave();
  await test4_crossCell();
  await test5_zigzag();
  await test6_highFreq();
  await test7_channelIsolation();
  await test8_multiSubscriber();
  await test9_lateJoin();
  await test10_disconnect();
  await test11_reconnect();
  await test12_redisRestartNote();
  await test13_100players();
  await test14_hotspot();
  await test15_multiNodeNote();

  console.log('ALL TESTS DONE');
}

run();