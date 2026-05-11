import { Injectable, Logger } from '@nestjs/common';
import { WebSocket } from 'ws';
import { ISessionService } from './session.interface';

@Injectable()
export class SessionService implements ISessionService {
  private readonly logger = new Logger(SessionService.name);

  private clientToUser = new Map<WebSocket, string>();
  private userToClient = new Map<string, WebSocket>();

  setSession(client: WebSocket, userId: string) {
    const existingClient = this.userToClient.get(userId);
    if (existingClient && existingClient !== client) {
      this.logger.warn(`Kicking existing session for user ${userId}`);
      existingClient.close(1000, 'Logged in from another device');
    }

    this.clientToUser.set(client, userId);
    this.userToClient.set(userId, client);
    this.logger.debug(`Session registered: ${userId}`);
  }

  getUserId(client: WebSocket): string | undefined {
    return this.clientToUser.get(client);
  }

  getClient(userId: string): WebSocket | undefined {
    return this.userToClient.get(userId);
  }

  removeSession(client: WebSocket) {
    const userId = this.clientToUser.get(client);
    if (userId) {
      if (this.userToClient.get(userId) === client) {
        this.userToClient.delete(userId);
      }
      this.clientToUser.delete(client);
      this.logger.debug(`Session removed: ${userId}`);
    }
  }

  getSessionCount(): number {
    return this.clientToUser.size;
  }
}
