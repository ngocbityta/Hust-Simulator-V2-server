import { getVietnamTimeComponents } from './time.util';

export function getDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371e3; // metres
  const phi1 = lat1 * Math.PI / 180;
  const phi2 = lat2 * Math.PI / 180;
  const deltaPhi = (lat2 - lat1) * Math.PI / 180;
  const deltaLambda = (lng2 - lng1) * Math.PI / 180;

  const a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
            Math.cos(phi1) * Math.cos(phi2) *
            Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
}

/**
 * Tính khoảng cách ngắn nhất (mét) từ điểm (lat, lng) đến đoạn thẳng AB.
 * Sử dụng phép chiếu vuông góc phẳng (planar projection) do khoảng cách nhỏ.
 */
export function distancePointToSegment(
  lat: number, lng: number,
  latA: number, lngA: number,
  latB: number, lngB: number
): number {
  // Đổi sang mét để tính trên hệ phẳng (Hà Nội: lat ~ 21 độ)
  const METERS_PER_LAT = 111000;
  const METERS_PER_LNG = 111000 * Math.cos((21.003 * Math.PI) / 180);

  const x = lng * METERS_PER_LNG;
  const y = lat * METERS_PER_LAT;
  const xA = lngA * METERS_PER_LNG;
  const yA = latA * METERS_PER_LAT;
  const xB = lngB * METERS_PER_LNG;
  const yB = latB * METERS_PER_LAT;

  const dx = xB - xA;
  const dy = yB - yA;
  const len2 = dx * dx + dy * dy;

  if (len2 === 0) {
    // A và B trùng nhau
    const dx2 = x - xA;
    const dy2 = y - yA;
    return Math.sqrt(dx2 * dx2 + dy2 * dy2);
  }

  // Tỷ lệ hình chiếu của điểm lên đường thẳng chứa đoạn AB
  let t = ((x - xA) * dx + (y - yA) * dy) / len2;
  t = Math.max(0, Math.min(1, t)); // Giới hạn t trong đoạn [0, 1]

  // Tìm điểm gần nhất trên đoạn AB
  const pX = xA + t * dx;
  const pY = yA + t * dy;

  const dx2 = x - pX;
  const dy2 = y - pY;
  return Math.sqrt(dx2 * dx2 + dy2 * dy2);
}

/**
 * Tính khoảng cách ngắn nhất (mét) từ điểm đến polyline.
 * polyline: mảng các [lng, lat]
 */
export function distancePointToPolyline(
  lat: number, lng: number,
  polyline: number[][]
): number {
  if (!polyline || polyline.length === 0) return Infinity;
  if (polyline.length === 1) {
    return getDistance(lat, lng, polyline[0][1], polyline[0][0]);
  }

  let minDist = Infinity;
  for (let i = 0; i < polyline.length - 1; i++) {
    const d = distancePointToSegment(
      lat, lng,
      polyline[i][1], polyline[i][0],
      polyline[i+1][1], polyline[i+1][0]
    );
    if (d < minDist) minDist = d;
  }
  return minDist;
}

export interface CampusPhaseInfo {
  transitRatio: number;
  phase: string;
  nodeHotspots: string[];
  wayDensityMultiplier: number;
}

export function getCampusPhase(targetTimestampMs?: number): CampusPhaseInfo {
  const timeMs = targetTimestampMs || Date.now();
  const { hour, dayOfWeek } = getVietnamTimeComponents(timeMs);

  if (dayOfWeek === 0 || dayOfWeek === 6) {
    return {
      transitRatio: 0.15,
      phase: 'WEEKEND',
      nodeHotspots: ['GATE', 'PARKING'],
      wayDensityMultiplier: 0.5,
    };
  }

  // 6:00 - 6:45 (6.0 - 6.75) Arriving for morning shifts
  if (hour >= 6.0 && hour < 6.75) {
    return { transitRatio: 0.75, phase: 'ARRIVING', nodeHotspots: ['GATE', 'PARKING'], wayDensityMultiplier: 1.5 };
  }
  
  // Ca 1: 6:45 - 8:20 (6.75 - 8.33)
  if (hour >= 6.75 && hour < 8.33) {
    return { transitRatio: 0.05, phase: 'IN_CLASS_CA1', nodeHotspots: [], wayDensityMultiplier: 0.3 };
  }
  
  // Shift change 1-2: 8:20 - 8:25 (8.33 - 8.41)
  if (hour >= 8.33 && hour < 8.41) {
    return { transitRatio: 0.65, phase: 'SHIFT_CHANGE_1_2', nodeHotspots: ['CANTEEN'], wayDensityMultiplier: 1.5 };
  }

  // Ca 2: 8:25 - 10:00 (8.41 - 10.00)
  if (hour >= 8.41 && hour < 10.00) {
    return { transitRatio: 0.05, phase: 'IN_CLASS_CA2', nodeHotspots: [], wayDensityMultiplier: 0.3 };
  }

  // Shift change 2-3: 10:00 - 10:05 (10.00 - 10.08)
  if (hour >= 10.00 && hour < 10.08) {
    return { transitRatio: 0.65, phase: 'SHIFT_CHANGE_2_3', nodeHotspots: ['CANTEEN'], wayDensityMultiplier: 1.5 };
  }

  // Ca 3: 10:05 - 11:40 (10.08 - 11.66)
  if (hour >= 10.08 && hour < 11.66) {
    return { transitRatio: 0.05, phase: 'IN_CLASS_CA3', nodeHotspots: [], wayDensityMultiplier: 0.3 };
  }

  // Lunch Rush: 11:40 - 12:15 (11.66 - 12.25)
  if (hour >= 11.66 && hour < 12.25) {
    return { transitRatio: 0.70, phase: 'LUNCH_RUSH', nodeHotspots: ['CANTEEN'], wayDensityMultiplier: 1.2 };
  }

  // Arriving/Settling for afternoon classes: 12:15 - 12:30 (12.25 - 12.50)
  if (hour >= 12.25 && hour < 12.50) {
    return { transitRatio: 0.40, phase: 'LUNCH_STAY_AND_ARRIVING', nodeHotspots: ['GATE', 'PARKING', 'CANTEEN'], wayDensityMultiplier: 0.8 };
  }

  // Ca 4: 12:30 - 14:05 (12.50 - 14.08)
  if (hour >= 12.50 && hour < 14.08) {
    return { transitRatio: 0.05, phase: 'IN_CLASS_CA4', nodeHotspots: [], wayDensityMultiplier: 0.3 };
  }

  // Shift change 4-5: 14:05 - 14:10 (14.08 - 14.16)
  if (hour >= 14.08 && hour < 14.16) {
    return { transitRatio: 0.65, phase: 'SHIFT_CHANGE_4_5', nodeHotspots: ['CANTEEN'], wayDensityMultiplier: 1.5 };
  }

  // Ca 5: 14:10 - 15:45 (14.16 - 15.75)
  if (hour >= 14.16 && hour < 15.75) {
    return { transitRatio: 0.05, phase: 'IN_CLASS_CA5', nodeHotspots: [], wayDensityMultiplier: 0.3 };
  }

  // Shift change 5-6: 15:45 - 15:50 (15.75 - 15.83)
  if (hour >= 15.75 && hour < 15.83) {
    return { transitRatio: 0.65, phase: 'SHIFT_CHANGE_5_6', nodeHotspots: ['CANTEEN'], wayDensityMultiplier: 1.5 };
  }

  // Ca 6: 15:50 - 17:25 (15.83 - 17.41)
  if (hour >= 15.83 && hour < 17.41) {
    return { transitRatio: 0.05, phase: 'IN_CLASS_CA6', nodeHotspots: [], wayDensityMultiplier: 0.3 };
  }

  // Departing: 17:25 - 18:00 (17.41 - 18.00)
  if (hour >= 17.41 && hour < 18.00) {
    return { transitRatio: 0.80, phase: 'DEPARTING', nodeHotspots: ['GATE', 'PARKING'], wayDensityMultiplier: 2.0 };
  }

  if (hour >= 18.0 && hour < 21.0) {
    return { transitRatio: 0.20, phase: 'EVENING', nodeHotspots: [], wayDensityMultiplier: 0.6 };
  }
  // hour >= 21.0 || hour < 6.0
  return { transitRatio: 0.10, phase: 'NIGHT', nodeHotspots: ['GATE'], wayDensityMultiplier: 0.2 };
}

/**
 * Ray-casting point-in-polygon test.
 * polygon: array of [lng, lat] pairs (same order as building coordinates).
 */
export function isPointInPolygon(testLng: number, testLat: number, polygon: number[][]): boolean {
  let inside = false;
  for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
    const xi = polygon[i][0], yi = polygon[i][1];
    const xj = polygon[j][0], yj = polygon[j][1];
    const intersect = ((yi > testLat) !== (yj > testLat)) &&
      (testLng < (xj - xi) * (testLat - yi) / (yj - yi) + xi);
    if (intersect) inside = !inside;
  }
  return inside;
}

/**
 * Interpolate evenly-spaced points along a polyline.
 * coords: [[lng, lat], ...], stepMeters: desired spacing.
 * Returns a new array of [lng, lat] points including originals + interpolated.
 */
export function interpolatePolyline(coords: number[][], stepMeters: number): number[][] {
  if (coords.length < 2) return [...coords];
  const result: number[][] = [coords[0]];

  for (let i = 0; i < coords.length - 1; i++) {
    const [lng1, lat1] = coords[i];
    const [lng2, lat2] = coords[i + 1];
    const segDist = getDistance(lat1, lng1, lat2, lng2);

    if (segDist > stepMeters) {
      const numSteps = Math.ceil(segDist / stepMeters);
      for (let s = 1; s < numSteps; s++) {
        const t = s / numSteps;
        result.push([
          lng1 + (lng2 - lng1) * t,
          lat1 + (lat2 - lat1) * t,
        ]);
      }
    }
    result.push(coords[i + 1]);
  }
  return result;
}

/**
 * Given a building polygon and its centroid, compute all grid cells that fall
 * inside the polygon, each with a Gaussian weight that peaks at the centroid
 * and falls off toward the edges.
 *
 * Returns an array of { cellX, cellY, weight } where weights are normalized
 * so they sum to 1.0.
 */
export function getPolygonCellsWithGaussian(
  polygon: number[][],          // [[lng, lat], ...]
  centroidLng: number,
  centroidLat: number,
  cellSize: number,             // meters
  metersPerLat: number,
  metersPerLng: number,
): Array<{ cellX: number; cellY: number; weight: number }> {
  // 1. Compute bounding box of polygon in cell coordinates
  let minLng = Infinity, maxLng = -Infinity;
  let minLat = Infinity, maxLat = -Infinity;
  for (const [lng, lat] of polygon) {
    if (lng < minLng) minLng = lng;
    if (lng > maxLng) maxLng = lng;
    if (lat < minLat) minLat = lat;
    if (lat > maxLat) maxLat = lat;
  }

  const minCellX = Math.floor((minLng * metersPerLng) / cellSize);
  const maxCellX = Math.floor((maxLng * metersPerLng) / cellSize);
  const minCellY = Math.floor((minLat * metersPerLat) / cellSize);
  const maxCellY = Math.floor((maxLat * metersPerLat) / cellSize);

  // 2. Compute max distance from centroid to polygon boundary (for sigma)
  let maxDist = 0;
  for (const [lng, lat] of polygon) {
    const d = getDistance(centroidLat, centroidLng, lat, lng);
    if (d > maxDist) maxDist = d;
  }
  // sigma = maxDist * 0.6 → ~85% of weight stays in inner 60% of building
  const sigma = Math.max(maxDist * 0.6, cellSize);

  // 3. Iterate all cells in bounding box, test point-in-polygon, compute Gaussian weight
  const cells: Array<{ cellX: number; cellY: number; weight: number }> = [];
  let totalWeight = 0;

  for (let cx = minCellX; cx <= maxCellX; cx++) {
    for (let cy = minCellY; cy <= maxCellY; cy++) {
      const cellCenterLng = ((cx + 0.5) * cellSize) / metersPerLng;
      const cellCenterLat = ((cy + 0.5) * cellSize) / metersPerLat;

      if (isPointInPolygon(cellCenterLng, cellCenterLat, polygon)) {
        const dist = getDistance(centroidLat, centroidLng, cellCenterLat, cellCenterLng);
        // Gaussian: exp(-d²/2σ²)
        const w = Math.exp(-(dist * dist) / (2 * sigma * sigma));
        cells.push({ cellX: cx, cellY: cy, weight: w });
        totalWeight += w;
      }
    }
  }

  // 4. Normalize weights to sum to 1.0
  cells.sort((a, b) => b.weight - a.weight);
  const maxCells = Math.min(50, Math.max(15, cells.length));
  const topCells = cells.slice(0, maxCells);
  
  let newTotal = 0;
  for (const cell of topCells) {
    newTotal += cell.weight;
  }

  if (newTotal > 0) {
    for (const cell of topCells) {
      cell.weight /= newTotal;
    }
  }

  return topCells;
}
