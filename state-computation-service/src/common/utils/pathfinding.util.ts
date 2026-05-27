import { Waypoint } from '../interfaces/heatmap.interface';
import { getDistance } from './geo.util';
import { Logger } from '@nestjs/common';

/**
 * Represents a node/edge from the context-service campus graph API.
 */
interface ApiNode {
  id: string;
  name: string;
  nodeType: string; // GATE, PARKING, CANTEEN, INTERSECTION, BUILDING
  latitude: number;
  longitude: number;
  buildingId?: string;
}

interface ApiEdge {
  id: string;
  fromNodeId: string;
  toNodeId: string;
  distanceMeters: number;
  isBidirectional: boolean;
}

interface ApiBuildingPOI {
  id: string; // UUID = db_uuid
  name: string;
  centroidLat: number;
  centroidLng: number;
}

interface FullGraphResponse {
  nodes: ApiNode[];
  edges: ApiEdge[];
  buildings: ApiBuildingPOI[];
}

interface GraphNode extends Waypoint {
  id: string;
  nodeType?: string;
}

export class CampusGraphData {
  nodes: Map<string, GraphNode> = new Map();
  gates: Waypoint[] = [];
  parkingAreas: Waypoint[] = [];
  canteenAreas: Waypoint[] = [];
  intersectionNames: Set<string> = new Set();
  // db_uuid -> { name, lat, lng }
  poisMap: Map<string, { name: string; lat: number; lng: number }> = new Map();
}

export class PathfindingUtil {
  private static instance: PathfindingUtil;
  private readonly logger = new Logger(PathfindingUtil.name);

  private nodes = new Map<string, GraphNode>();
  private adjacencyList = new Map<string, Array<{ nodeId: string; weight: number }>>();
  private precomputedPaths = new Map<string, Map<string, Waypoint[]>>();

  private graphData = new CampusGraphData();
  private initialized = false;

  private contextServiceHost: string;
  private contextServicePort: number;

  private constructor(host: string, port: number) {
    this.contextServiceHost = host;
    this.contextServicePort = port;
  }

  public static getInstance(host = 'localhost', port = 8080): PathfindingUtil {
    if (!PathfindingUtil.instance) {
      PathfindingUtil.instance = new PathfindingUtil(host, port);
    }
    return PathfindingUtil.instance;
  }

  /**
   * Fetch the full graph (nodes, edges, buildings) from context-service and build the routing graph.
   * Must be called once on startup.
   */
  public async initialize(): Promise<void> {
    if (this.initialized) return;

    try {
      const url = `http://${this.contextServiceHost}:${this.contextServicePort}/api/campus-graph/full`;
      this.logger.log(`Fetching campus graph from ${url}...`);

      const res = await fetch(url);
      if (!res.ok) {
        throw new Error(`Failed to fetch campus graph: ${res.statusText}`);
      }
      const data: FullGraphResponse = await res.json();

      // 1. Load explicit graph nodes (gates, parking, canteens, intersections)
      for (const node of data.nodes) {
        const graphNode: GraphNode = {
          id: node.id,
          name: node.name,
          lat: node.latitude,
          lng: node.longitude,
          nodeType: node.nodeType,
        };
        this.nodes.set(node.name, graphNode);
        this.adjacencyList.set(node.name, []);
        this.graphData.nodes.set(node.name, graphNode);

        // Categorize
        switch (node.nodeType) {
          case 'GATE':
            this.graphData.gates.push({ lat: node.latitude, lng: node.longitude, name: node.name });
            break;
          case 'PARKING':
            this.graphData.parkingAreas.push({ lat: node.latitude, lng: node.longitude, name: node.name });
            break;
          case 'CANTEEN':
            this.graphData.canteenAreas.push({ lat: node.latitude, lng: node.longitude, name: node.name });
            break;
          case 'INTERSECTION':
            this.graphData.intersectionNames.add(node.name);
            break;
        }
      }

      // 2. Load edges (undirected)
      for (const edge of data.edges) {
        const fromNode = data.nodes.find(n => n.id === edge.fromNodeId);
        const toNode = data.nodes.find(n => n.id === edge.toNodeId);
        if (!fromNode || !toNode) continue;

        const dist = edge.distanceMeters || getDistance(fromNode.latitude, fromNode.longitude, toNode.latitude, toNode.longitude);

        this.adjacencyList.get(fromNode.name)?.push({ nodeId: toNode.name, weight: dist });
        if (edge.isBidirectional) {
          this.adjacencyList.get(toNode.name)?.push({ nodeId: fromNode.name, weight: dist });
        }
      }

      // 3. Load buildings (POIs) and connect each to nearest explicit node
      const explicitNodesArray = Array.from(this.nodes.values());

      for (const building of data.buildings) {
        const poiId = building.id;
        this.graphData.poisMap.set(poiId, {
          name: building.name,
          lat: building.centroidLat,
          lng: building.centroidLng,
        });

        this.nodes.set(poiId, {
          id: poiId,
          name: building.name,
          lat: building.centroidLat,
          lng: building.centroidLng,
        });
        this.adjacencyList.set(poiId, []);

        // Connect to nearest explicit node
        let nearestNode = explicitNodesArray[0];
        let minD = Infinity;
        for (const n of explicitNodesArray) {
          const d = getDistance(building.centroidLat, building.centroidLng, n.lat, n.lng);
          if (d < minD) {
            minD = d;
            nearestNode = n;
          }
        }

        this.adjacencyList.get(poiId)!.push({ nodeId: nearestNode.name, weight: minD });
        this.adjacencyList.get(nearestNode.name)!.push({ nodeId: poiId, weight: minD });
      }

      this.initialized = true;
      this.logger.log(
        `Campus graph loaded: ${data.nodes.length} infrastructure nodes, ${data.edges.length} edges, ${data.buildings.length} buildings.`,
      );
    } catch (err) {
      this.logger.error(`Failed to initialize PathfindingUtil: ${err}`);
      throw err;
    }
  }

  public isInitialized(): boolean {
    return this.initialized;
  }

  public getGraphData(): CampusGraphData {
    return this.graphData;
  }

  /**
   * Returns array of waypoints representing the shortest path.
   */
  public getPath(startId: string, endId: string): Waypoint[] {
    if (!this.initialized) return [];
    if (startId === endId) {
      const node = this.nodes.get(startId);
      return node ? [{ lat: node.lat, lng: node.lng, name: node.name }] : [];
    }

    // Check cache
    if (this.precomputedPaths.has(startId) && this.precomputedPaths.get(startId)!.has(endId)) {
      return this.precomputedPaths.get(startId)!.get(endId)!;
    }

    if (!this.nodes.has(startId) || !this.nodes.has(endId)) {
      return [];
    }

    const pathIds = this.dijkstra(startId, endId);
    const waypoints = pathIds.map(id => {
      const n = this.nodes.get(id)!;
      return { lat: n.lat, lng: n.lng, name: n.name, nodeType: n.nodeType };
    });

    // Cache both directions
    if (!this.precomputedPaths.has(startId)) {
      this.precomputedPaths.set(startId, new Map());
    }
    this.precomputedPaths.get(startId)!.set(endId, waypoints);

    const revWaypoints = [...waypoints].reverse();
    if (!this.precomputedPaths.has(endId)) {
      this.precomputedPaths.set(endId, new Map());
    }
    this.precomputedPaths.get(endId)!.set(startId, revWaypoints);

    return waypoints;
  }

  private dijkstra(startId: string, endId: string): string[] {
    const distances = new Map<string, number>();
    const previous = new Map<string, string | null>();
    const unvisited = new Set<string>();

    for (const nodeId of this.nodes.keys()) {
      distances.set(nodeId, Infinity);
      previous.set(nodeId, null);
      unvisited.add(nodeId);
    }
    distances.set(startId, 0);

    while (unvisited.size > 0) {
      let currentId: string | null = null;
      let minDist = Infinity;
      for (const nodeId of unvisited) {
        const d = distances.get(nodeId)!;
        if (d < minDist) {
          minDist = d;
          currentId = nodeId;
        }
      }

      if (currentId === null || currentId === endId) break;

      unvisited.delete(currentId);

      const neighbors = this.adjacencyList.get(currentId) || [];
      for (const neighbor of neighbors) {
        if (!unvisited.has(neighbor.nodeId)) continue;
        const alt = distances.get(currentId)! + neighbor.weight;
        if (alt < distances.get(neighbor.nodeId)!) {
          distances.set(neighbor.nodeId, alt);
          previous.set(neighbor.nodeId, currentId);
        }
      }
    }

    const path: string[] = [];
    let curr: string | null = endId;
    if (previous.get(curr) !== null || curr === startId) {
      while (curr !== null) {
        path.unshift(curr);
        curr = previous.get(curr)!;
      }
    }
    return path;
  }

  /**
   * Find the name of the nearest transit node (gate/parking/canteen).
   */
  public getNearestTransitNode(lat: number, lng: number, list: Waypoint[]): string {
    let minD = Infinity;
    let nearestId = list[0]?.name || '';
    for (const item of list) {
      const d = getDistance(lat, lng, item.lat, item.lng);
      if (d < minD) {
        minD = d;
        nearestId = item.name;
      }
    }
    return nearestId;
  }

  public isIntersection(nodeName?: string): boolean {
    return !!nodeName && this.graphData.intersectionNames.has(nodeName);
  }

  /**
   * Check if a waypoint is a bottleneck node (gate, parking, or intersection) using DB-driven nodeType.
   */
  public isBottleneck(waypoint: { nodeType?: string }): boolean {
    const t = waypoint.nodeType;
    return t === 'GATE' || t === 'PARKING' || t === 'INTERSECTION';
  }
}
