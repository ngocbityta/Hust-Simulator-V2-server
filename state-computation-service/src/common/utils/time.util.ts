import { ActivityKeyframe, interpolateKeyframes } from '../constants/activity-profile.constant';

export const VIETNAM_OFFSET_MS = 7 * 60 * 60 * 1000;

/**
 * Converts a target timestamp to Vietnam timezone (UTC+7) components: decimal hour and day of week.
 * Docker containers usually run in UTC, so this ensures local time is correctly simulated.
 */
export function getVietnamTimeComponents(targetTimestampMs: number): { hour: number; dayOfWeek: number } {
  const d = new Date(targetTimestampMs + VIETNAM_OFFSET_MS);
  const hour = d.getUTCHours() + d.getUTCMinutes() / 60;
  const dayOfWeek = d.getUTCDay(); // 0 = Sunday, 6 = Saturday
  return { hour, dayOfWeek };
}

/**
 * Calculates a time-based activity multiplier for crowd simulation based on keyframes and weekend factor.
 */
export function calculateActivityMultiplier(
  targetTimestampMs: number,
  keyframes: ActivityKeyframe[],
  weekendFactor: number
): number {
  const { hour, dayOfWeek } = getVietnamTimeComponents(targetTimestampMs);
  let multiplier = interpolateKeyframes(hour, keyframes);

  // Weekends: apply weekend multiplier factor (default 30% of weekday traffic)
  if (dayOfWeek === 0 || dayOfWeek === 6) {
    multiplier *= weekendFactor;
  }

  return multiplier;
}
