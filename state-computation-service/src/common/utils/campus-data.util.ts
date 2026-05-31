import { Logger } from '@nestjs/common';

export interface ApiNode {
  id: string;
  name: string;
  nodeType: string;
  latitude: number;
  longitude: number;
  buildingId?: string;
}

export interface ApiWay {
  id: string;
  name: string;
  wayType: string;
  coordinates: number[][]; // [[lng, lat], ...]
  distanceMeters: number;
  isOneway: boolean;
}

export interface BuildingPolygon {
  id: string;
  name: string;
  polygon: number[][];        // [[lng, lat], ...]
  centroidLat: number;
  centroidLng: number;
}

export interface CampusData {
  nodes: Map<string, ApiNode>;
  ways: ApiWay[];
  gates: ApiNode[];
  parkingAreas: ApiNode[];
  canteenAreas: ApiNode[];
  buildingPolygons: Map<string, BuildingPolygon>;
}

export class CampusDataUtil {
  private static instance: CampusDataUtil;
  private readonly logger = new Logger(CampusDataUtil.name);

  private data: CampusData = {
    nodes: new Map(),
    ways: [],
    gates: [],
    parkingAreas: [],
    canteenAreas: [],
    buildingPolygons: new Map(),
  };
  private initialized = false;

  private contextServiceHost: string;
  private contextServicePort: number;

  private constructor(host: string, port: number) {
    this.contextServiceHost = host;
    this.contextServicePort = port;
  }

  public static getInstance(host = 'localhost', port = 8080): CampusDataUtil {
    if (!CampusDataUtil.instance) {
      CampusDataUtil.instance = new CampusDataUtil(host, port);
    }
    return CampusDataUtil.instance;
  }

  public async initialize(): Promise<void> {
    if (this.initialized) return;

    try {
      this.logger.log(`Fetching campus data from ${this.contextServiceHost}:${this.contextServicePort}...`);

      // 1. Fetch nodes
      const nodesRes = await fetch(`http://${this.contextServiceHost}:${this.contextServicePort}/api/campus-nodes`);
      if (!nodesRes.ok) throw new Error(`Failed to fetch nodes: ${nodesRes.statusText}`);
      const nodes: ApiNode[] = await nodesRes.json();

      // 2. Fetch ways
      const waysRes = await fetch(`http://${this.contextServiceHost}:${this.contextServicePort}/api/campus-ways`);
      if (!waysRes.ok) throw new Error(`Failed to fetch ways: ${waysRes.statusText}`);
      const ways: ApiWay[] = await waysRes.json();

      // Process nodes
      this.data.nodes.clear();
      this.data.gates = [];
      this.data.parkingAreas = [];
      this.data.canteenAreas = [];

      for (const node of nodes) {
        this.data.nodes.set(node.id, node);
        switch (node.nodeType) {
          case 'GATE':
            this.data.gates.push(node);
            break;
          case 'PARKING':
            this.data.parkingAreas.push(node);
            break;
          case 'CANTEEN':
            this.data.canteenAreas.push(node);
            break;
        }
      }

      this.data.ways = ways;

      // 3. Fetch buildings for polygon data
      try {
        const buildingsRes = await fetch(`http://${this.contextServiceHost}:${this.contextServicePort}/api/buildings/active`);
        if (buildingsRes.ok) {
          const buildings: Array<{ id: string; name: string; coordinates: string; centroidLat: number | null; centroidLng: number | null }> = await buildingsRes.json();
          this.data.buildingPolygons.clear();
          for (const b of buildings) {
            if (b.centroidLat == null || b.centroidLng == null) continue;
            try {
              const polygon = JSON.parse(b.coordinates) as number[][];
              if (Array.isArray(polygon) && polygon.length >= 3) {
                this.data.buildingPolygons.set(b.id, {
                  id: b.id,
                  name: b.name,
                  polygon,
                  centroidLat: b.centroidLat,
                  centroidLng: b.centroidLng,
                });
              }
            } catch { /* skip invalid coordinates */ }
          }
          this.logger.log(`Loaded ${this.data.buildingPolygons.size} building polygons.`);
        }
      } catch (err) {
        this.logger.warn(`Could not load building polygons: ${err}`);
      }

      this.initialized = true;

      this.logger.log(
        `Campus data loaded: ${nodes.length} nodes, ${ways.length} ways, ${this.data.buildingPolygons.size} building polygons.`
      );
    } catch (err) {
      this.logger.error(`Failed to initialize CampusDataUtil: ${err}`);
      throw err;
    }
  }

  public isInitialized(): boolean {
    return this.initialized;
  }

  public getData(): CampusData {
    return this.data;
  }
}
