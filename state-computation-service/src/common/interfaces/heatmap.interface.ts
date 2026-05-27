export interface PredictiveHeatmapCell {
  cellX: number;
  cellY: number;
  count: number;
  centerLat: number;
  centerLng: number;
  intents: Record<string, number>;
}

export interface PredictiveHeatmapPayload {
  timestamp: number;
  totalOnline: number;
  cells: PredictiveHeatmapCell[];
}

export interface ActiveEvent {
  id: string;
  name: string;
  type: string;
  buildingId?: string;
  estimatedParticipants?: number;
}

export interface Waypoint {
  lat: number;
  lng: number;
  name: string;
  nodeType?: string;
}
