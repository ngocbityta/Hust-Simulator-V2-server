package com.hustsimulator.context.room;

import java.util.UUID;

/**
 * Data Transfer Objects for Room operations.
 */
public class RoomDTO {

    public record CreateRoomRequest(String name, UUID buildingId) {}

    public record UpdateRoomRequest(String name) {}
}
