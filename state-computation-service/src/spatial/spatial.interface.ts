export interface GridCell {
  x: number;
  y: number;
}

export interface ISpatialService {
  getGridCell(latitude: number, longitude: number): GridCell;
  getCellKey(cell: GridCell): string;
  getAoiCells(centerCell: GridCell): GridCell[];
  getCellChannel(cell: GridCell): string;
  getCellSize(): number;
  getMetersPerLat(): number;
  getMetersPerLng(): number;
  getZoneId(longitude: number): number;
  isBorderLongitude(longitude: number): boolean;
}

export const ISpatialService = Symbol('ISpatialService');
