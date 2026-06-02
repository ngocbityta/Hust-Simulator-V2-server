import { ISpatialService } from '../../spatial/spatial.interface';

/**
 * Phân bổ trọng số của những người di chuyển trên đường theo hiệu ứng Perlin-Noise 1D
 * Điều này tạo ra từng cụm tụ tập (moving clusters/flocking).
 */
export function distributeFlockingWeight(
  interpolatedCoords: number[][],
  weightForThisWay: number,
  targetTimestampMs: number | undefined,
  spatialService: ISpatialService,
  cellMap: Map<string, any>
): number {
  let wayCellsAdded = 0;
  const timeOffset = (targetTimestampMs || Date.now()) / 8000.0;
  const noiseArray = new Float32Array(interpolatedCoords.length);
  let totalNoise = 0;

  for (let i = 0; i < interpolatedCoords.length; i++) {
     const wave1 = Math.sin(i * 0.6 - timeOffset);
     const wave2 = Math.sin(i * 0.25 - timeOffset * 1.2);
     let noiseVal = (wave1 * 0.5 + wave2 * 0.5) + 1.0; 
     noiseVal = Math.pow(noiseVal, 3);
     
     noiseArray[i] = noiseVal;
     totalNoise += noiseVal;
  }

  for (let i = 0; i < interpolatedCoords.length; i++) {
     const coord = interpolatedCoords[i];
     const lng = coord[0];
     const lat = coord[1];
     const targetCell = spatialService.getGridCell(lat, lng);
     const cellKey = spatialService.getCellKey(targetCell);

     if (!cellMap.has(cellKey)) {
        cellMap.set(cellKey, { cellX: targetCell.x, cellY: targetCell.y, count: 0, intents: new Map<string, number>() });
     }

     const weightPerPoint = totalNoise > 0 ? weightForThisWay * (noiseArray[i] / totalNoise) : 0;
     
     if (weightPerPoint < 0.005) continue; 

     const cellData = cellMap.get(cellKey)!;
     cellData.count += weightPerPoint;
     cellData.intents.set('[Transit] Trên đường', (cellData.intents.get('[Transit] Trên đường') || 0) + weightPerPoint);
     wayCellsAdded++;
  }
  
  return wayCellsAdded;
}
