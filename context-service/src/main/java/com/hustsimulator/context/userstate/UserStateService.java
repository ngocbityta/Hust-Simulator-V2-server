package com.hustsimulator.context.userstate;

import com.hustsimulator.context.entity.UserState;
import com.hustsimulator.context.enums.UserActivityState;
import java.util.List;
import java.util.UUID;

public interface UserStateService {
    UserState findByUserId(UUID userId);
    List<UserState> findByActivityState(UserActivityState state);
    List<UserState> findByMapId(UUID mapId);
    List<UserState> findByEventId(UUID eventId);
    UserState changeMap(UUID userId, UUID mapId);
    UserState leaveMap(UUID userId);
    UserState joinBuilding(UUID userId, UUID buildingId, Double userX, Double userY);
    UserState leaveBuilding(UUID userId);
    UserState joinRoom(UUID userId, UUID roomId);
    UserState leaveRoom(UUID userId);
    UserState joinEvent(UUID userId, UUID eventId, UUID buildingId, Double userX, Double userY);
    UserState leaveEvent(UUID userId);
    UserState joinRecurringEvent(UUID userId, UUID eventId, UUID buildingId, Double userX, Double userY);
    UserState updateActivity(UUID userId, UserActivityState activityState, String sessionData);
    UserState syncPositionState(UUID userId, double x, double y);
}
