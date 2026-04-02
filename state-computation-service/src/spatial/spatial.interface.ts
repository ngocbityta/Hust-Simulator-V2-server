export interface GridCell {
  x: number;
  y: number;
}

export interface ISpatialService {
  getGridCell(latitude: number, longitude: number): GridCell;
  getCellKey(cell: GridCell): string;
  getAoiCells(centerCell: GridCell): GridCell[];
  getCellChannel(cell: GridCell): string;
}

export const ISpatialService = Symbol('ISpatialService');
