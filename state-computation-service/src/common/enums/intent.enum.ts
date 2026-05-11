/**
 * Intent-related constants and enums.
 */

export enum IntentType {
  GOING_TO_EVENT = 'GOING_TO_EVENT',
  WANDERING = 'WANDERING',
  STATIONARY = 'STATIONARY',
}

export enum IntentPayloadKey {
  LATITUDE = 'latitude',
  LONGITUDE = 'longitude',
  BUILDING_NAME = 'buildingName',
  ROOM_NAME = 'roomName',
}
