export interface ActivityKeyframe {
  hour: number;
  multiplier: number;
}

export const DEFAULT_WEEKDAY_PROFILE: ActivityKeyframe[] = [
  { hour: 0.0, multiplier: 0.05 },
  { hour: 6.0, multiplier: 0.05 },
  { hour: 7.0, multiplier: 0.85 },
  { hour: 9.25, multiplier: 1.0 }, // Peak of morning curve: 0.85 + 0.15 = 1.0
  { hour: 11.5, multiplier: 0.85 },
  { hour: 11.6, multiplier: 0.55 }, // Lunch drop transition
  { hour: 13.0, multiplier: 0.55 },
  { hour: 15.0, multiplier: 1.0 }, // Peak of afternoon curve: 0.8 + 0.2 = 1.0
  { hour: 17.0, multiplier: 0.8 },
  { hour: 18.0, multiplier: 0.6 },
  { hour: 21.0, multiplier: 0.2 },
  { hour: 24.0, multiplier: 0.1 },
];

export function interpolateKeyframes(hour: number, keyframes: ActivityKeyframe[]): number {
  if (!keyframes || keyframes.length === 0) return 1.0;
  
  // Sort keyframes just in case they are not in order
  const sorted = [...keyframes].sort((a, b) => a.hour - b.hour);

  // 1. Clip out of bounds hours
  if (hour <= sorted[0].hour) return sorted[0].multiplier;
  if (hour >= sorted[sorted.length - 1].hour) return sorted[sorted.length - 1].multiplier;

  // 2. Find interval and linearly interpolate
  for (let i = 0; i < sorted.length - 1; i++) {
    const k1 = sorted[i];
    const k2 = sorted[i + 1];
    if (hour >= k1.hour && hour <= k2.hour) {
      if (k2.hour === k1.hour) return k1.multiplier;
      const t = (hour - k1.hour) / (k2.hour - k1.hour);
      return k1.multiplier + (k2.multiplier - k1.multiplier) * t;
    }
  }
  
  return sorted[0].multiplier;
}
