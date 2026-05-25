package com.hustsimulator.context.userstate;

import com.hustsimulator.context.enums.UserActivityState;
import com.hustsimulator.context.entity.UserState;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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

    @PutMapping("/map")
    @Operation(summary = "Change map", description = "Transfer a player to a different virtual map")
    public UserState changeMap(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader, @RequestBody UserStateDTO.ChangeMapRequest request) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.changeMap(userId, request.mapId());
    }

    @PutMapping("/leave-map")
    public UserState leaveMap(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.leaveMap(userId);
    }

    // --- Building ---

    @PutMapping("/building")
    @Operation(summary = "Join building", description = "Update player state when entering a physical building area")
    public UserState joinBuilding(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader, @RequestBody UserStateDTO.JoinBuildingRequest request) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.joinBuilding(userId, request.buildingId(), request.userX(), request.userY());
    }

    @PutMapping("/leave-building")
    public UserState leaveBuilding(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.leaveBuilding(userId);
    }

    // --- Room ---

    @PutMapping("/room")
    public UserState joinRoom(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader, @RequestBody UserStateDTO.JoinRoomRequest request) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.joinRoom(userId, request.roomId());
    }

    @PutMapping("/leave-room")
    public UserState leaveRoom(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.leaveRoom(userId);
    }

    // --- Event ---

    @PutMapping("/event")
    public UserState joinEvent(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader, @RequestBody UserStateDTO.JoinEventRequest request) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.joinEvent(userId, request.eventId(), request.buildingId(), request.userX(), request.userY());
    }

    @PutMapping("/leave-event")
    public UserState leaveEvent(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.leaveEvent(userId);
    }

    // --- Recurring Event ---

    @PutMapping("/recurring-event")
    public UserState joinRecurringEvent(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader, @RequestBody UserStateDTO.JoinRecurringEventRequest request) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.joinRecurringEvent(userId, request.eventId(), request.buildingId(), request.userX(), request.userY());
    }

    // --- Activity ---

    @PutMapping("/activity")
    @Operation(summary = "Update activity state", description = "Directly modify a user's activity state (ROAMING, SPECTATING, etc.)")
    public UserState updateActivity(@Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader, @RequestBody UserStateDTO.UpdateActivityRequest request) {
        UUID userId = resolveUserId(userIdHeader);
        return userStateService.updateActivity(userId, request.activityState(), request.sessionData());
    }

    private UUID resolveUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid X-User-Id format");
        }
    }
}
