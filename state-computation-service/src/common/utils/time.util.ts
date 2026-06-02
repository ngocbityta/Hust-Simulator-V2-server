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
