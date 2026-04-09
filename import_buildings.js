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
            }
        };

        const req = http.request(options, (res) => {
            let body = '';
            res.on('data', (chunk) => body += chunk);
            res.on('end', () => {
                if (res.statusCode >= 200 && res.statusCode < 300) {
                    resolve(body ? JSON.parse(body) : null);
                } else {
                    reject(new Error(`Status: ${res.statusCode}, Body: ${body}`));
                }
            });
        });

        req.on('error', reject);
        if (data) req.write(JSON.stringify(data));
        req.end();
    });
}

async function main() {
    console.log('--- Starting Building Import ---');

    // 1. Ensure Virtual Map exists
    let maps = await request('/maps', 'GET');
    let hustMap = maps.find(m => m.name === 'HUST Campus');

    if (!hustMap) {
        console.log('Creating HUST Campus map...');
        hustMap = await request('/maps', 'POST', {
            name: 'HUST Campus',
            type: 'CAMPUS',
            radius: 1000,
            metadata: JSON.stringify({ description: 'Main campus map' }),
            isActive: true
        });
    }
    console.log(`Using Map ID: ${hustMap.id}`);

    // 2. Read buildings.json
    const buildingsData = JSON.parse(fs.readFileSync('/Users/mac/Documents/HustSimulatorV2/server/state-dissemination-service/buildings.json', 'utf8'));
    console.log(`Found ${buildingsData.length} buildings in JSON.`);

    // 3. Batch Import
    for (const b of buildingsData) {
        try {
            // Map JSON coordinates [[lng, lat], ...] to PointDto {x, y}
            const points = b.coordinates.map(coord => ({
                x: coord[0],
                y: coord[1]
            }));

            const payload = {
                name: b.name || `Building ${b.id}`,
                mapId: hustMap.id,
                points: points
            };

            await request('/buildings', 'POST', payload);
            console.log(`Imported: ${payload.name}`);
        } catch (err) {
            console.error(`Failed to import building ${b.id}: ${err.message}`);
        }
    }

    console.log('--- Import Completed ---');
}

main().catch(console.error);
