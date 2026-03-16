import { WebSocket } from 'ws';

export interface IInterestService {
  updateInterests(client: WebSocket, newAoiKeys: Set<string>): void;
  getClientsInCell(cellKey: string): Set<WebSocket> | undefined;
  removeClient(client: WebSocket): void;
}

export const IInterestService = Symbol('IInterestService');
