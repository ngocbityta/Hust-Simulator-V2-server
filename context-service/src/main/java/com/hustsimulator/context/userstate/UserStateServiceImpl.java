package com.hustsimulator.context.userstate;

import com.hustsimulator.context.building.BuildingService;
import com.hustsimulator.context.enums.UserActivityState;
import com.hustsimulator.context.entity.UserState;
import com.hustsimulator.context.map.MapService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserStateServiceImpl implements UserStateService {

    private final UserStateRepository userStateRepository;
    private final BuildingService buildingService;
    private final MapService mapService;

    @Override
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

    @Override
    public List<UserState> findByActivityState(UserActivityState state) {
        return userStateRepository.findByActivityState(state);
    }

    @Override
    public List<UserState> findByMapId(UUID mapId) {
        return userStateRepository.findByMapId(mapId);
    }

    @Override
    public List<UserState> findByEventId(UUID eventId) {
        return userStateRepository.findByEventId(eventId);
    }

    @Override
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

    @Override
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

    @Override
    public UserState joinBuilding(UUID userId, UUID buildingId, Double userX, Double userY) {
        UserState state = findByUserId(userId);
        boolean isInside = isPhysicallyInBuilding(buildingId, userX, userY);

        state.setBuildingId(buildingId);
        state.setRoomId(null);
        state.setEventId(null);
        state.setActivityState(isInside ? UserActivityState.IN_BUILDING : UserActivityState.SPECTATING_BUILDING);
        state.setEnteredAt(LocalDateTime.now());

        log.info("User {} {} building {}", userId, isInside ? "entered" : "spectating", buildingId);
        return userStateRepository.save(state);
    }

    @Override
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

    @Override
    public UserState joinRoom(UUID userId, UUID roomId) {
        UserState state = findByUserId(userId);
        if (state.getActivityState() != UserActivityState.IN_BUILDING
                && state.getActivityState() != UserActivityState.IN_ROOM) {
            throw new IllegalStateException("User must be IN_BUILDING to join a room. Current state: " + state.getActivityState());
        }
        state.setRoomId(roomId);
        state.setActivityState(UserActivityState.IN_ROOM);
        state.setEnteredAt(LocalDateTime.now());
        log.info("User {} entered room {}", userId, roomId);
        return userStateRepository.save(state);
    }

    @Override
    public UserState leaveRoom(UUID userId) {
        UserState state = findByUserId(userId);
        state.setRoomId(null);
        state.setEventId(null);
        state.setActivityState(UserActivityState.IN_BUILDING);
        state.setEnteredAt(LocalDateTime.now());
        log.info("User {} left room", userId);
        return userStateRepository.save(state);
    }

    @Override
    public UserState joinEvent(UUID userId, UUID eventId, UUID buildingId, Double userX, Double userY) {
        UserState state = findByUserId(userId);
        boolean isInside = isPhysicallyInBuilding(buildingId, userX, userY);

        state.setEventId(eventId);
        state.setActivityState(isInside ? UserActivityState.IN_EVENT : UserActivityState.SPECTATING_EVENT);
        state.setEnteredAt(LocalDateTime.now());

        log.info("User {} {} event {}", userId, isInside ? "joining" : "spectating", eventId);
        return userStateRepository.save(state);
    }

    @Override
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

    @Override
    public UserState joinRecurringEvent(UUID userId, UUID eventId, UUID buildingId, Double userX, Double userY) {
        UserState state = findByUserId(userId);
        boolean isInside = isPhysicallyInBuilding(buildingId, userX, userY);

        state.setEventId(eventId);
        state.setActivityState(isInside ? UserActivityState.IN_RECURRING_EVENT : UserActivityState.SPECTATING_RECURRING_EVENT);
        state.setEnteredAt(LocalDateTime.now());

        log.info("User {} {} recurring event {}", userId, isInside ? "joining" : "spectating", eventId);
        return userStateRepository.save(state);
    }

    @Override
    public UserState updateActivity(UUID userId, UserActivityState activityState, String sessionData) {
        UserState state = findByUserId(userId);
        state.setActivityState(activityState);
        if (sessionData != null) {
            state.setSessionData(sessionData);
        }
        log.info("User {} activity changed to {}", userId, activityState);
        return userStateRepository.save(state);
    }

    @Override
    public UserState syncPositionState(UUID userId, double x, double y) {
        UserState state = findByUserId(userId);
        List<com.hustsimulator.context.entity.Map> maps = mapService.findActiveMaps();
        
        UUID currentMapId = maps.stream()
                .filter(m -> mapService.isPointInsideMap(m.getId(), x, y))
                .map(com.hustsimulator.context.entity.Map::getId)
                .findFirst()
                .orElse(null);
        
        if (currentMapId == null) {
            if (state.getActivityState() != UserActivityState.OUTSIDE_MAP) {
                log.info("User {} moved outside of all maps", userId);
                state.setActivityState(UserActivityState.OUTSIDE_MAP);
                state.setMapId(null);
                state.setBuildingId(null);
                state.setRoomId(null);
                state.setEventId(null);
                state.setEnteredAt(LocalDateTime.now());
            }
        } else {
            if (state.getActivityState() == UserActivityState.OUTSIDE_MAP) {
                log.info("User {} entered map {}", userId, currentMapId);
                state.setActivityState(UserActivityState.ROAMING);
                state.setMapId(currentMapId);
                state.setEnteredAt(LocalDateTime.now());
            } else if (state.getMapId() == null || !state.getMapId().equals(currentMapId)) {
                state.setMapId(currentMapId);
                log.info("User {} moved to map {}", userId, currentMapId);
            }
        }
        
        return userStateRepository.save(state);
    }

    private boolean isPhysicallyInBuilding(UUID buildingId, Double x, Double y) {
        if (buildingId == null || x == null || y == null) return false;
        return buildingService.isPointInsideBuilding(buildingId, x, y);
    }
}
