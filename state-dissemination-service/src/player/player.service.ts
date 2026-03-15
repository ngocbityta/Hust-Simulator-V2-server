import { Injectable, Logger } from '@nestjs/common';

export enum UserActivityState {
    ROAMING = 'ROAMING',
    IN_RECURRING_EVENT = 'IN_RECURRING_EVENT',
    IN_EVENT = 'IN_EVENT',
}

interface UserState {
    userId: string;
    username?: string;
    avatar?: string;
    position: { latitude: number; longitude: number };
    speed: number;
    heading: number;
    isOnline: boolean;
    activityState: UserActivityState;
    currentMapId?: string;
    currentEventId?: string;
    sessionData?: Record<string, unknown>;
    lastUpdate: number;
}

@Injectable()
export class PlayerService {
    private readonly logger = new Logger(PlayerService.name);

    // In-memory player state (will be backed by Redis later)
    private players = new Map<string, UserState>();

    updatePosition(
        userId: string,
        position: { latitude: number; longitude: number },
        speed: number,
        heading: number,
    ): void {
        const existing = this.players.get(userId);
        this.players.set(userId, {
            ...existing,
            userId,
            position,
            speed,
            heading,
            isOnline: true,
            activityState: existing?.activityState ?? UserActivityState.ROAMING,
            lastUpdate: Date.now(),
        });
    }

    updateActivityState(
        userId: string,
        state: UserActivityState,
        mapId?: string,
        eventId?: string,
        sessionData?: Record<string, unknown>,
    ): { success: boolean; message: string } {
        const existing = this.players.get(userId);
        if (!existing) {
            this.logger.warn(`Cannot update activity state: user ${userId} not found`);
            return { success: false, message: 'User not found' };
        }

        existing.activityState = state;
        existing.currentMapId = mapId;
        existing.currentEventId = eventId;
        existing.sessionData = sessionData;
        existing.lastUpdate = Date.now();

        this.logger.log(`User ${userId} state changed to ${state}`);
        return { success: true, message: 'OK' };
    }

    getNearbyPlayers(
        userId: string,
        position: { latitude: number; longitude: number },
        radius: number,
    ) {
        // Simple distance-based filtering (Haversine formula)
        // TODO: Replace with spatial index (QuadTree) for production
        const nearby: UserState[] = [];

        this.players.forEach((player) => {
            if (player.userId === userId || !player.isOnline) return;

            const distance = this.haversineDistance(
                position.latitude,
                position.longitude,
                player.position.latitude,
                player.position.longitude,
            );

            if (distance <= radius) {
                nearby.push(player);
            }
        });

        return {
            players: nearby.map((p) => ({
                userId: p.userId,
                username: p.username || '',
                avatar: p.avatar || '',
                position: p.position,
                isOnline: p.isOnline,
                activityState: p.activityState,
            })),
        };
    }

    handleConnectionEvent(event: {
        userId: string;
        isConnected: boolean;
        timestamp: { millis: number };
    }) {
        if (event.isConnected) {
            const existing = this.players.get(event.userId);
            if (existing) {
                existing.isOnline = true;
            }
        } else {
            const existing = this.players.get(event.userId);
            if (existing) {
                existing.isOnline = false;
            }
        }

        this.logger.log(
            `User ${event.userId} is now ${event.isConnected ? 'online' : 'offline'}`,
        );

        return { success: true, message: 'OK' };
    }

    private haversineDistance(
        lat1: number,
        lng1: number,
        lat2: number,
        lng2: number,
    ): number {
        const R = 6371000; // Earth radius in meters
        const dLat = this.toRad(lat2 - lat1);
        const dLng = this.toRad(lng2 - lng1);
        const a =
            Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(this.toRad(lat1)) *
            Math.cos(this.toRad(lat2)) *
            Math.sin(dLng / 2) *
            Math.sin(dLng / 2);
        const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    private toRad(deg: number): number {
        return (deg * Math.PI) / 180;
    }
}
