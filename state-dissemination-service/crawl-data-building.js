const axios = require("axios");
const fs = require("fs");

const OVERPASS_URL = "https://overpass-api.de/api/interpreter";

const query = `
[out:json][timeout:25];
area["name"="Đại học Bách khoa Hà Nội"]->.a;
(
  way["building"](area.a);
);
out body;
>;
out skel qt;
`;

function isClockwise(coords) {
    let sum = 0;
    for (let i = 0; i < coords.length - 1; i++) {
        const [x1, y1] = coords[i];
        const [x2, y2] = coords[i + 1];
        sum += (x2 - x1) * (y2 + y1);
    }
    return sum > 0;
}

function normalizeClockwise(coords) {
    if (!isClockwise(coords)) {
        return coords.reverse();
    }
    return coords;
}

async function crawlAndSave() {
    try {
        const res = await axios.post(OVERPASS_URL, query);
        const data = res.data;

        // map nodeId -> tọa độ
        const nodesMap = {};
        data.elements.forEach(el => {
            if (el.type === "node") {
                nodesMap[el.id] = [el.lon, el.lat];
            }
        });

        // build buildings
        const buildings = data.elements
            .filter(el => el.type === "way" && el.tags?.building)
            .map(way => {
                let coords = way.nodes
                    .map(id => nodesMap[id])
                    .filter(Boolean);

                // ensure closed polygon
                if (coords.length > 0) {
                    const first = coords[0];
                    const last = coords[coords.length - 1];
                    if (first[0] !== last[0] || first[1] !== last[1]) {
                        coords.push(first);
                    }
                }

                coords = normalizeClockwise(coords);

                return {
                    id: way.id,
                    name: way.tags.name || `building_${way.id}`,
                    tags: way.tags,
                    coordinates: coords
                };
            });

        console.log("Total buildings:", buildings.length);

        // 🔥 GHI FILE
        fs.writeFileSync(
            "buildings.json",
            JSON.stringify(buildings, null, 2),
            "utf-8"
        );

        console.log("✅ Saved to buildings.json");

    } catch (err) {
        console.error(err);
    }
}

crawlAndSave();