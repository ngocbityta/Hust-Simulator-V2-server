package com.hustsimulator.context.userstate;

import com.hustsimulator.context.building.BuildingService;
import com.hustsimulator.context.enums.UserActivityState;
import com.hustsimulator.context.entity.UserState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserStateService {

    private final UserStateRepository userStateRepository;
    private final BuildingService buildingService;

    public UserState findByUserId(UUID userId) {
        return userStateRepository.findByUserId(userId).orElseGet(() -> {
            log.info("UserState not found for user {}, creating default", userId);
            UserState state = UserState.builder()
                    .userId(userId)
                    .activityState(UserActivityState.OUTSIDE_MAP)
                    .sessionData("{}")
                    .enteredAt(LocalDateTime.now())
                    .build();
            return userStateRepository.save(state);
        });
    }

    public List<UserState> findByActivityState(UserActivityState state) {
        return userStateRepository.findByActivityState(state);
    }

    public List<UserState> findByMapId(UUID mapId) {
        return userStateRepository.findByMapId(mapId);
    }

    public List<UserState> findByEventId(UUID eventId) {
        return userStateRepository.findByEventId(eventId);
    }

    /**
     * Enter a map — sets the user to ROAMING on that map.
     * Clears building, room, and event context.
     */
    public UserState changeMap(UUID userId, UUID mapId) {
        UserState state = findByUserId(userId);
        state.setMapId(mapId);
        state.setEventId(null);
        state.setBuildingId(null);
        state.setRoomId(null);
        state.setActivityState(UserActivityState.ROAMING);
        state.setEnteredAt(LocalDateTime.now());
        log.info("User {} changing map to {}", userId, mapId);
        return userStateRepository.save(state);
    }

    /**
     * Leave the current map — sets the user to OUTSIDE_MAP.
     */
    public UserState leaveMap(UUID userId) {
        UserState state = findByUserId(userId);
        state.setMapId(null);
        state.setBuildingId(null);
        state.setRoomId(null);
        state.setEventId(null);
        state.setActivityState(UserActivityState.OUTSIDE_MAP);
        state.setEnteredAt(LocalDateTime.now());
        log.info("User {} left map", userId);
        return userStateRepository.save(state);
    }

    public UserState joinBuilding(UUID userId, UUID buildingId, Double userX, Double userY) {
        UserState state = findByUserId(userId);

        boolean isInside = false;
        if (userX != null && userY != null) {
            isInside = buildingService.isPointInsideBuilding(buildingId, userX, userY);
        }

        state.setBuildingId(buildingId);
        state.setRoomId(null);
        state.setEventId(null);

        if (isInside) {
            state.setActivityState(UserActivityState.IN_BUILDING);
            log.info("User {} entered building {} (physically inside)", userId, buildingId);
        } else {
            state.setActivityState(UserActivityState.SPECTATING_BUILDING);
            log.info("User {} spectating building {} (not physically inside, x={}, y={})",
                    userId, buildingId, userX, userY);
        }

        state.setEnteredAt(LocalDateTime.now());
        return userStateRepository.save(state);
    }

    public UserState leaveBuilding(UUID userId) {
        UserState state = findByUserId(userId);
        state.setBuildingId(null);
        state.setRoomId(null);
        state.setEventId(null);
        state.setActivityState(UserActivityState.ROAMING);
        state.setEnteredAt(LocalDateTime.now());
        log.info("User {} left building", userId);
        return userStateRepository.save(state);
    }

    public UserState joinRoom(UUID userId, UUID roomId) {
        UserState state = findByUserId(userId);
        if (state.getActivityState() != UserActivityState.IN_BUILDING
                && state.getActivityState() != UserActivityState.IN_ROOM) {
            throw new IllegalStateException(
                    "User must be IN_BUILDING to join a room. Current state: " + state.getActivityState());
        }
        state.setRoomId(roomId);
        state.setActivityState(UserActivityState.IN_ROOM);
        state.setEnteredAt(LocalDateTime.now());
        log.info("User {} entered room {}", userId, roomId);
        return userStateRepository.save(state);
    }

    public UserState leaveRoom(UUID userId) {
        UserState state = findByUserId(userId);
        state.setRoomId(null);
        state.setEventId(null);
        state.setActivityState(UserActivityState.IN_BUILDING);
        state.setEnteredAt(LocalDateTime.now());
        log.info("User {} left room", userId);
        return userStateRepository.save(state);
    }

    public UserState joinEvent(UUID userId, UUID eventId, UUID buildingId, Double userX, Double userY) {
        UserState state = findByUserId(userId);

        boolean isInside = false;
        if (buildingId != null && userX != null && userY != null) {
            isInside = buildingService.isPointInsideBuilding(buildingId, userX, userY);
        }

        state.setEventId(eventId);

        if (isInside) {
            state.setActivityState(UserActivityState.IN_EVENT);
            log.info("User {} joining event {} (physically present)", userId, eventId);
        } else {
            state.setActivityState(UserActivityState.SPECTATING_EVENT);
            log.info("User {} spectating event {} (not physically present)", userId, eventId);
        }

        state.setEnteredAt(LocalDateTime.now());
        return userStateRepository.save(state);
    }

    public UserState leaveEvent(UUID userId) {
        UserState state = findByUserId(userId);
        state.setEventId(null);

        if (state.getRoomId() != null) {
            state.setActivityState(UserActivityState.IN_ROOM);
        } else if (state.getBuildingId() != null) {
            state.setActivityState(UserActivityState.IN_BUILDING);
        } else {
            state.setActivityState(UserActivityState.ROAMING);
        }

        state.setEnteredAt(LocalDateTime.now());
        log.info("User {} left event", userId);
        return userStateRepository.save(state);
    }

    public UserState joinRecurringEvent(UUID userId, UUID eventId, UUID buildingId, Double userX, Double userY) {
        UserState state = findByUserId(userId);

        boolean isInside = false;
        if (buildingId != null && userX != null && userY != null) {
            isInside = buildingService.isPointInsideBuilding(buildingId, userX, userY);
        }

        state.setEventId(eventId);

        if (isInside) {
            state.setActivityState(UserActivityState.IN_RECURRING_EVENT);
            log.info("User {} joining recurring event {} (physically present)", userId, eventId);
        } else {
            state.setActivityState(UserActivityState.SPECTATING_RECURRING_EVENT);
            log.info("User {} spectating recurring event {} (not physically present)", userId, eventId);
        }

        state.setEnteredAt(LocalDateTime.now());
        return userStateRepository.save(state);
    }

    public UserState updateActivity(UUID userId, UserActivityState activityState, String sessionData) {
        UserState state = findByUserId(userId);
        state.setActivityState(activityState);
        if (sessionData != null) {
            state.setSessionData(sessionData);
        }
        log.info("User {} activity changed to {}", userId, activityState);
        return userStateRepository.save(state);
    }
}
