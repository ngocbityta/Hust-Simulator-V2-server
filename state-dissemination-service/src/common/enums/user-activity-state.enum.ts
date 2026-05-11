export enum UserActivityState {
  ROAMING = 'ROAMING',                               // proto = 1
  IN_RECURRING_EVENT = 'IN_RECURRING_EVENT',           // proto = 2
  IN_EVENT = 'IN_EVENT',                               // proto = 3
  OUTSIDE_MAP = 'OUTSIDE_MAP',                         // proto = 4
  IN_BUILDING = 'IN_BUILDING',                         // proto = 5
  SPECTATING_BUILDING = 'SPECTATING_BUILDING',         // proto = 6
  IN_ROOM = 'IN_ROOM',                                 // proto = 7
  SPECTATING_EVENT = 'SPECTATING_EVENT',               // proto = 8
  SPECTATING_RECURRING_EVENT = 'SPECTATING_RECURRING_EVENT', // proto = 9
}
