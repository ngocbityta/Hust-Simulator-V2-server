const WebSocket = require('ws');

const WS_URL = 'ws://localhost:3002/ws';
const NUM_USERS = 10;
const DURATION_MS = 10000; // 10 seconds
const INTERVAL_MS = 200;   // 5 updates per second per user

async function runStressTest() {
  console.log(`--- STARTING 10-USER CONTINUOUS MOVEMENT TEST ---`);
  
  const clients = [];
  const messageCounts = new Array(NUM_USERS).fill(0);

  // Initial positions around HUST
  const startLat = 21.003;
  const startLng = 105.843;

  for (let i = 0; i < NUM_USERS; i++) {
    const ws = new WebSocket(WS_URL);
    const userId = `stress-user-${i}`;
    
    await new Promise(res => ws.on('open', res));
    
    ws.on('message', (msg) => {
      const data = JSON.parse(msg);
      if (data.event === 'user:state_update') {
        messageCounts[i]++;
      }
    });

    // Join
    ws.send(JSON.stringify({ event: 'user:join', data: { userId } }));
    
    clients.push({ ws, userId, lat: startLat + (i * 0.0001), lng: startLng + (i * 0.0001) });
  }

  console.log(`${NUM_USERS} users connected and joined.`);

  const startTime = Date.now();
  const interval = setInterval(() => {
    clients.forEach((c, idx) => {
      // Random walk
      c.lat += (Math.random() - 0.5) * 0.0001;
      c.lng += (Math.random() - 0.5) * 0.0001;
      
      c.ws.send(JSON.stringify({
        event: 'user:move',
        data: {
          position: { latitude: c.lat, longitude: c.lng },
          speed: 1,
          heading: Math.random() * 360
        }
      }));
    });

    if (Date.now() - startTime > DURATION_MS) {
      clearInterval(interval);
      summarize();
    }
  }, INTERVAL_MS);

  function summarize() {
    console.log('\n--- TEST SUMMARY ---');
    let totalMessages = 0;
    clients.forEach((c, i) => {
      console.log(`User ${c.userId}: received ${messageCounts[i]} updates`);
      totalMessages += messageCounts[i];
      c.ws.close();
    });
    console.log(`\nTotal updates disseminated: ${totalMessages}`);
    console.log(`Average updates/user: ${totalMessages / NUM_USERS}`);
    console.log('--- TEST COMPLETED ---');
    process.exit(0);
  }
}

runStressTest().catch(console.error);
