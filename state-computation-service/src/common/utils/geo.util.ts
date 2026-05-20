import { Waypoint } from '../interfaces/heatmap.interface';
import { getVietnamTimeComponents } from './time.util';

export function getDistance(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371e3; // metres
  const phi1 = lat1 * Math.PI / 180;
  const phi2 = lat2 * Math.PI / 180;
  const deltaPhi = (lat2 - lat1) * Math.PI / 180;
  const deltaLambda = (lng2 - lng1) * Math.PI / 180;

  const a = Math.sin(deltaPhi / 2) * Math.sin(deltaPhi / 2) +
            Math.cos(phi1) * Math.cos(phi2) *
            Math.sin(deltaLambda / 2) * Math.sin(deltaLambda / 2);
  const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

  return R * c;
}

export function findNearest(lat: number, lng: number, list: Waypoint[]): Waypoint {
  let minD = Infinity;
  let nearest = list[0];
  for (const item of list) {
    const d = getDistance(lat, lng, item.lat, item.lng);
    if (d < minD) {
      minD = d;
      nearest = item;
    }
  }
  return nearest;
}

export function interpolatePoints(
  lat1: number, lng1: number,
  lat2: number, lng2: number,
  steps: number
): Array<{ lat: number, lng: number }> {
  const points: Array<{ lat: number, lng: number }> = [];
  for (let i = 0; i <= steps; i++) {
    const t = i / steps;
    points.push({
      lat: lat1 + (lat2 - lat1) * t,
      lng: lng1 + (lng2 - lng1) * t
    });
  }
  return points;
}

export function getCampusPhase(targetTimestampMs?: number): { transitRatio: number, phase: string } {
  if (!targetTimestampMs) {
    return { transitRatio: 0.0, phase: 'LIVE_REALTIME' };
  }
  const { hour, dayOfWeek } = getVietnamTimeComponents(targetTimestampMs);

  if (dayOfWeek === 0 || dayOfWeek === 6) {
    return { transitRatio: 0.1, phase: 'WEEKEND' };
  }

  if (hour >= 6.0 && hour < 7.0) {
    return { transitRatio: 0.8, phase: 'ARRIVING' };
  }
  if (hour >= 7.0 && hour < 7.5) {
    return { transitRatio: 0.3, phase: 'SETTLING' };
  }
  if (hour >= 7.5 && hour < 11.5) {
    return { transitRatio: 0.05, phase: 'IN_CLASS' };
  }
  if (hour >= 11.5 && hour < 13.0) {
    return { transitRatio: 0.6, phase: 'LUNCH_BREAK' };
  }
  if (hour >= 13.0 && hour < 13.5) {
    return { transitRatio: 0.3, phase: 'SETTLING' };
  }
  if (hour >= 13.5 && hour < 17.0) {
    return { transitRatio: 0.05, phase: 'IN_CLASS' };
  }
  if (hour >= 17.0 && hour < 18.0) {
    return { transitRatio: 0.8, phase: 'DEPARTING' };
  }
  if (hour >= 18.0 && hour < 21.0) {
    return { transitRatio: 0.3, phase: 'EVENING' };
  }
  return { transitRatio: 0.1, phase: 'NIGHT' };
}
