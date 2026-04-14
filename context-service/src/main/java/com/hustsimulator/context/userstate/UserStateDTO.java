package com.hustsimulator.context.userstate;

import com.hustsimulator.context.enums.UserActivityState;
import java.util.UUID;

/**
 * Data Transfer Objects for User State operations.
 */
public class UserStateDTO {

    public record ChangeMapRequest(UUID mapId) {}

    public record JoinBuildingRequest(UUID buildingId, Double userX, Double userY) {}

    public record JoinRoomRequest(UUID roomId) {}

    public record JoinEventRequest(UUID eventId, UUID buildingId, Double userX, Double userY) {}

    public record JoinRecurringEventRequest(UUID eventId, UUID buildingId, Double userX, Double userY) {}

    public record UpdateActivityRequest(UserActivityState activityState, String sessionData) {}
}
