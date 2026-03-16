const WebSocket = require('ws');

const WS_URL = 'ws://localhost:3002/ws';

async function testAOI() {
  console.log('--- STARTING AOI DISSEMINATION TEST ---');

  // Player 1: Center position
  const p1 = new WebSocket(WS_URL);
  const p1Id = 'user-1';
  const p1Pos = { latitude: 21.003, longitude: 105.843 }; // Near HUST

  // Player 2: Close to Player 1 (same cell or AOI)
  const p2 = new WebSocket(WS_URL);
  const p2Id = 'user-2';
  const p2Pos = { latitude: 21.0031, longitude: 105.8431 }; // ~15m away

  // Player 3: Far from Player 1
  const p3 = new WebSocket(WS_URL);
  const p3Id = 'user-3';
  const p3Pos = { latitude: 21.100, longitude: 105.900 }; // ~15km away

  let messagesP2 = 0;
  let messagesP3 = 0;

  const waitForOpen = (ws) => new Promise(res => ws.on('open', res));

  await Promise.all([waitForOpen(p1), waitForOpen(p2), waitForOpen(p3)]);
  console.log('All clients connected');

  p2.on('message', (msg) => {
    const data = JSON.parse(msg);
    if (data.event === 'user:state_update' && data.data.userId === p1Id) {
      console.log('[P2] Received update from P1:', data.data.type);
      messagesP2++;
    }
  });

  p3.on('message', (msg) => {
    const data = JSON.parse(msg);
    if (data.event === 'user:state_update' && data.data.userId === p1Id) {
      console.log('[P3] Received update from P1 (ERROR):', data.data.type);
      messagesP3++;
    }
  });

  // 1. All join and wait for ACK
  const join = (ws, id) => new Promise(res => {
    const handler = (msg) => {
      const data = JSON.parse(msg.toString());
      if (data.event === 'user:joined' && data.data.userId === id) {
        ws.off('message', handler);
        res();
      }
    };
    ws.on('message', handler);
    ws.send(JSON.stringify({ event: 'user:join', data: { userId: id } }));
  });

  await Promise.all([join(p1, p1Id), join(p2, p2Id), join(p3, p3Id)]);
  console.log('All clients joined successfully');

  // 2. All initialize positions (so they subscribe to cells)
  p1.send(JSON.stringify({ event: 'user:move', data: { position: p1Pos, speed: 0, heading: 0 } }));
  p2.send(JSON.stringify({ event: 'user:move', data: { position: p2Pos, speed: 0, heading: 0 } }));
  p3.send(JSON.stringify({ event: 'user:move', data: { position: p3Pos, speed: 0, heading: 0 } }));

  await new Promise(r => setTimeout(r, 1000));

  // 3. Player 1 moves again
  console.log('Player 1 moving...');
  p1.send(JSON.stringify({ event: 'user:move', data: { 
    position: { latitude: 21.00305, longitude: 105.84305 }, 
    speed: 1, 
    heading: 45 
  } }));

  await new Promise(r => setTimeout(r, 1000));

  console.log(`Summary: P2 received ${messagesP2} updates, P3 received ${messagesP3} updates.`);

  if (messagesP2 > 0 && messagesP3 === 0) {
    console.log('SUCCESS: AOI filtering works correctly!');
  } else {
    console.log('FAILURE: AOI filtering failed.');
  }

  p1.close();
  p2.close();
  p3.close();
  process.exit(messagesP2 > 0 && messagesP3 === 0 ? 0 : 1);
}

testAOI().catch(err => {
  console.error(err);
  process.exit(1);
});
