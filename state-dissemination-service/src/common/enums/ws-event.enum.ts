export enum WsEvent {
  // Incoming
  USER_JOIN = 'user:join',
  USER_MOVE = 'user:move',
  USER_STATE_CHANGE = 'user:state_change',

  // Outgoing
  USER_JOINED = 'user:joined',
  USER_MOVED_ACK = 'user:moved_ack',
  USER_STATE_CHANGED_ACK = 'user:state_changed_ack',
  USER_ERROR = 'user:error',
  USER_STATE_ERROR = 'user:state_error',
  USER_STATE_UPDATE = 'user:state_update',
}
