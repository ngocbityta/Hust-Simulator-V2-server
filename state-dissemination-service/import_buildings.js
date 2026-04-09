const fs = require('fs');
const http = require('http');

const API_BASE = 'http://localhost:8080/api';

async function request(path, method, data) {
    return new Promise((resolve, reject) => {
        const url = new URL(`${API_BASE}${path}`);
        const options = {
            hostname: url.hostname,
            port: url.port,
            path: url.pathname,
            method: method,
            headers: {
                'Content-Type': 'application/json',
            },
            timeout: 10000 // 10 seconds timeout
        };

        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', (chunk) => body += chunk);
            res.on('end', () => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    try {
                        resolve(body ? JSON.parse(body) : null);
                    } catch (e) {
                        resolve(body);
                    }
                } else {
                    reject(new Error(`Status: ${res.statusCode}, Body: ${body}`));
                }
            });
        });

        req.on('error', reject);
        req.on('timeout', () => {
            req.destroy();
            reject(new Error('Request Timeout'));
        });

        if (data) req.write(JSON.stringify(data));
        req.end();
    });
}

async function main() {
    console.log('--- Starting Optimized HUST Data Import ---');

    try {
        // 1. Ensure Map exists
        console.log('Checking for map "Đại Học Bách Khoa Hà Nội"...');
        let maps = await request('/maps', 'GET');
        let hustMap = maps.find(m => m.name === 'Đại Học Bách Khoa Hà Nội');

        if (!hustMap) {
            console.log('Creating Đại Học Bách Khoa Hà Nội map...');
            hustMap = await request('/maps', 'POST', {
                name: 'Đại Học Bách Khoa Hà Nội',
                coordinates: JSON.stringify([
                    [105.8400, 21.0076],
                    [105.8470, 21.0076],
                    [105.8470, 21.0020],
                    [105.8400, 21.0020]
                ]),
                isActive: true
            });
        }
        console.log(`Using Map ID: ${hustMap.id}`);

        // 2. Read buildings.json
        const buildingsPath = '/Users/mac/Documents/HustSimulatorV2/server/state-dissemination-service/buildings.json';
        const buildingsData = JSON.parse(fs.readFileSync(buildingsPath, 'utf8'));
        console.log(`Found ${buildingsData.length} buildings in JSON.`);

        // 3. Sequential Import
        let buildingCount = 0;
        for (const b of buildingsData) {
            buildingCount++;
            try {
                const buildingPayload = {
                    name: b.name || `Building ${b.id}`,
                    mapId: hustMap.id,
                    points: b.coordinates
                };

                const createdBuilding = await request('/buildings', 'POST', buildingPayload);
                process.stdout.write(`\r[${buildingCount}/${buildingsData.length}] Importing: ${createdBuilding.name.padEnd(30)}`);

                // Create rooms sequentially to prevent socket exhaustion
                const roomNames = [];
                for (let i = 101; i <= 120; i++) roomNames.push(i.toString());
                for (let i = 501; i <= 520; i++) roomNames.push(i.toString());

                for (const roomName of roomNames) {
                    await request('/rooms', 'POST', {
                        name: roomName,
                        buildingId: createdBuilding.id
                    });
                }

            } catch (err) {
                console.error(`\nFailed to import building ${b.id}: ${err.message}`);
            }
        }

        console.log('\n--- HUST Data Import Successfully Completed ---');
    } catch (err) {
        console.error('Fatal Error during import:', err.message);
    }
}

main().catch(console.error);
