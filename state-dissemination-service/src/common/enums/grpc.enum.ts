export enum GrpcService {
  CONTEXT_ENGINE_SERVICE = 'ContextEngineService',
  USER_STATE_SERVICE = 'UserStateService',
}

export enum GrpcMethodName {
  // Context Service (Client side)
  CHECK_PLAYER_ZONE = 'checkPlayerZone',
  REPORT_SPATIAL_TRIGGER = 'reportSpatialTrigger',
  UPDATE_PLAYER_STATE = 'updatePlayerState',

  // Dissemination Service (Server side)
  GET_NEARBY_USERS = 'GetNearbyUsers',
  NOTIFY_USER_CONNECTION = 'NotifyUserConnection',
}
