import { WebSocket } from 'ws';

export interface ISessionService {
  setSession(client: WebSocket, userId: string): void;
  getUserId(client: WebSocket): string | undefined;
  getClient(userId: string): WebSocket | undefined;
  removeSession(client: WebSocket): void;
  getSessionCount(): number;
}

export const ISessionService = Symbol('ISessionService');
