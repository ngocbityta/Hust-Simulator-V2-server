package com.hustsimulator.social.follow;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/follows")
@RequiredArgsConstructor
@Tag(name = "Follows", description = "Follow / unfollow user operations")
public class FollowController {

    private final FollowService followService;

    @Operation(summary = "Follow a user")
    @PostMapping
    public Map<String, Object> follow(@RequestBody FollowDTO.FollowRequest request,
                                      @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        followService.follow(userId, request.targetUserId());
        return Map.of(
                "targetUserId", request.targetUserId(),
                "following", true
        );
    }

    @Operation(summary = "Unfollow a user")
    @DeleteMapping("/{targetUserId}")
    public Map<String, Object> unfollow(@PathVariable UUID targetUserId,
                                        @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        followService.unfollow(userId, targetUserId);
        return Map.of(
                "targetUserId", targetUserId,
                "following", false
        );
    }

    @Operation(summary = "Get followers of a user (paginated)")
    @GetMapping("/followers/{userId}")
    public Page<FollowDTO.FollowResponse> getFollowers(@PathVariable UUID userId,
                                                        Pageable pageable) {
        return followService.getFollowers(userId, pageable);
    }

    @Operation(summary = "Get users that a user is following (paginated)")
    @GetMapping("/following/{userId}")
    public Page<FollowDTO.FollowResponse> getFollowing(@PathVariable UUID userId,
                                                        Pageable pageable) {
        return followService.getFollowing(userId, pageable);
    }

    @Operation(summary = "Get follower and following counts for a user")
    @GetMapping("/count/{userId}")
    public FollowDTO.FollowCountResponse getFollowCounts(@PathVariable UUID userId) {
        return followService.getFollowCounts(userId);
    }

    @Operation(summary = "Check if the current user is following another user")
    @GetMapping("/is-following/{targetUserId}")
    public Map<String, Object> isFollowing(@PathVariable UUID targetUserId,
                                           @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return Map.of(
                "targetUserId", targetUserId,
                "isFollowing", followService.isFollowing(userId, targetUserId)
        );
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
