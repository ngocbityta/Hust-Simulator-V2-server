package com.hustsimulator.context.userstate;

import com.hustsimulator.context.enums.UserActivityState;
import com.hustsimulator.context.entity.UserState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/user-states")
@RequiredArgsConstructor
@Tag(name = "User State API", description = "Management of player sessions, activity states, and spatial contexts")
public class UserStateController {

    private final UserStateService userStateService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user state", description = "Retrieve current activity, map, and session data for a player")
    public UserState findByUserId(@PathVariable UUID userId) {
        return userStateService.findByUserId(userId);
    }

    @GetMapping("/activity/{state}")
    public List<UserState> findByActivityState(@PathVariable UserActivityState state) {
        return userStateService.findByActivityState(state);
    }

    @GetMapping("/map/{mapId}")
    public List<UserState> findByMapId(@PathVariable UUID mapId) {
        return userStateService.findByMapId(mapId);
    }

    @GetMapping("/event/{eventId}")
    public List<UserState> findByEventId(@PathVariable UUID eventId) {
        return userStateService.findByEventId(eventId);
    }

    // --- Map ---

    @PutMapping("/{userId}/map")
    @Operation(summary = "Change map", description = "Transfer a player to a different virtual map")
    public UserState changeMap(@PathVariable UUID userId, @RequestBody ChangeMapRequest request) {
        return userStateService.changeMap(userId, request.mapId());
    }

    @PutMapping("/{userId}/leave-map")
    public UserState leaveMap(@PathVariable UUID userId) {
        return userStateService.leaveMap(userId);
    }

    // --- Building ---

    @PutMapping("/{userId}/building")
    @Operation(summary = "Join building", description = "Update player state when entering a physical building area")
    public UserState joinBuilding(@PathVariable UUID userId, @RequestBody JoinBuildingRequest request) {
        return userStateService.joinBuilding(userId, request.buildingId(), request.userX(), request.userY());
    }

    @PutMapping("/{userId}/leave-building")
    public UserState leaveBuilding(@PathVariable UUID userId) {
        return userStateService.leaveBuilding(userId);
    }

    // --- Room ---

    @PutMapping("/{userId}/room")
    public UserState joinRoom(@PathVariable UUID userId, @RequestBody JoinRoomRequest request) {
        return userStateService.joinRoom(userId, request.roomId());
    }

    @PutMapping("/{userId}/leave-room")
    public UserState leaveRoom(@PathVariable UUID userId) {
        return userStateService.leaveRoom(userId);
    }

    // --- Event ---

    @PutMapping("/{userId}/event")
    public UserState joinEvent(@PathVariable UUID userId, @RequestBody JoinEventRequest request) {
        return userStateService.joinEvent(userId, request.eventId(), request.buildingId(), request.userX(), request.userY());
    }

    @PutMapping("/{userId}/leave-event")
    public UserState leaveEvent(@PathVariable UUID userId) {
        return userStateService.leaveEvent(userId);
    }

    // --- Recurring Event ---

    @PutMapping("/{userId}/recurring-event")
    public UserState joinRecurringEvent(@PathVariable UUID userId, @RequestBody JoinRecurringEventRequest request) {
        return userStateService.joinRecurringEvent(userId, request.eventId(), request.buildingId(), request.userX(), request.userY());
    }

    // --- Activity ---

    @PutMapping("/{userId}/activity")
    @Operation(summary = "Update activity state", description = "Directly modify a user's activity state (ROAMING, SPECTATING, etc.)")
    public UserState updateActivity(@PathVariable UUID userId, @RequestBody UpdateActivityRequest request) {
        return userStateService.updateActivity(userId, request.activityState(), request.sessionData());
    }

    // --- DTOs ---

    public record ChangeMapRequest(UUID mapId) {}
    public record JoinBuildingRequest(UUID buildingId, Double userX, Double userY) {}
    public record JoinRoomRequest(UUID roomId) {}
    public record JoinEventRequest(UUID eventId, UUID buildingId, Double userX, Double userY) {}
    public record JoinRecurringEventRequest(UUID eventId, UUID buildingId, Double userX, Double userY) {}
    public record UpdateActivityRequest(UserActivityState activityState, String sessionData) {}
}
