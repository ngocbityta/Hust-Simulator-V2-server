package com.hustsimulator.social.friend;

import com.hustsimulator.social.enums.FriendshipStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/friends")
@RequiredArgsConstructor
@Tag(name = "Friends", description = "Friend request and friendship management")
public class FriendController {

    private final FriendService friendService;

    @Operation(summary = "Send a friend request")
    @PostMapping("/request")
    public FriendDTO.FriendResponse sendRequest(@RequestBody FriendDTO.SendFriendRequest request,
                                                 @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return friendService.sendRequest(userId, request.targetUserId());
    }

    @Operation(summary = "Accept a friend request")
    @PostMapping("/accept/{friendshipId}")
    public FriendDTO.FriendResponse acceptRequest(@PathVariable UUID friendshipId,
                                                   @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return friendService.acceptRequest(userId, friendshipId);
    }

    @Operation(summary = "Reject a friend request")
    @PostMapping("/reject/{friendshipId}")
    public Map<String, Object> rejectRequest(@PathVariable UUID friendshipId,
                                              @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        friendService.rejectRequest(userId, friendshipId);
        return Map.of("message", "Friend request rejected");
    }

    @Operation(summary = "Unfriend a user")
    @DeleteMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unfriend(@PathVariable UUID targetUserId,
                         @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        friendService.unfriend(userId, targetUserId);
    }

    @Operation(summary = "Get friends list (paginated)")
    @GetMapping
    public Page<FriendDTO.FriendListResponse> getFriends(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader,
            Pageable pageable) {
        UUID userId = resolveUserId(userIdHeader);
        return friendService.getFriends(userId, pageable);
    }

    @Operation(summary = "Get pending friend requests")
    @GetMapping("/requests")
    public Page<FriendDTO.FriendResponse> getPendingRequests(
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader,
            Pageable pageable) {
        UUID userId = resolveUserId(userIdHeader);
        return friendService.getPendingRequests(userId, pageable);
    }

    @Operation(summary = "Check friendship status with another user")
    @GetMapping("/status/{targetUserId}")
    public FriendDTO.FriendshipStatusResponse getFriendshipStatus(@PathVariable UUID targetUserId,
                                                    @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return friendService.getFriendshipStatus(userId, targetUserId);
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
