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

  if (hour >= 6.0 && hour < 7.0) {
    return { transitRatio: 0.75, phase: 'ARRIVING', nodeHotspots: ['GATE', 'PARKING'], wayDensityMultiplier: 1.5 };
  }
  if (hour >= 7.0 && hour < 7.5) {
    return { transitRatio: 0.25, phase: 'SETTLING', nodeHotspots: ['GATE', 'PARKING'], wayDensityMultiplier: 1.2 };
  }
  if (hour >= 7.5 && hour < 11.5) {
    return { transitRatio: 0.05, phase: 'IN_CLASS', nodeHotspots: [], wayDensityMultiplier: 0.3 };
  }
  if (hour >= 11.5 && hour < 12.0) {
    return { transitRatio: 0.70, phase: 'LUNCH_RUSH', nodeHotspots: ['CANTEEN'], wayDensityMultiplier: 1.2 };
  }
  if (hour >= 12.0 && hour < 12.75) { // 12:00 - 12:45
    return { transitRatio: 0.15, phase: 'LUNCH_STAY', nodeHotspots: ['CANTEEN'], wayDensityMultiplier: 0.8 };
  }
  if (hour >= 12.75 && hour < 13.5) { // 12:45 - 13:30
    return { transitRatio: 0.45, phase: 'POST_LUNCH', nodeHotspots: ['CANTEEN'], wayDensityMultiplier: 1.0 };
  }
  if (hour >= 13.5 && hour < 17.0) {
    return { transitRatio: 0.05, phase: 'IN_CLASS', nodeHotspots: [], wayDensityMultiplier: 0.3 };
  }
  if (hour >= 17.0 && hour < 17.5) { // 17:00 - 17:30
    return { transitRatio: 0.80, phase: 'DEPARTING', nodeHotspots: ['GATE', 'PARKING'], wayDensityMultiplier: 2.0 };
  }
  if (hour >= 17.5 && hour < 18.0) { // 17:30 - 18:00
    return { transitRatio: 0.55, phase: 'LATE_DEPART', nodeHotspots: ['GATE', 'PARKING'], wayDensityMultiplier: 1.5 };
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
  if (totalWeight > 0) {
    for (const cell of cells) {
      cell.weight /= totalWeight;
    }
  }

  return cells;
}
