package com.hustsimulator.social.follow;

import java.util.UUID;

/**
 * Data Transfer Objects for Follow operations.
 */
public class FollowDTO {

    public record FollowRequest(UUID targetUserId) {}

    public record FollowResponse(
            UUID userId,
            String username,
            String avatar,
            boolean isFollowing
    ) {}

    public record FollowCountResponse(
            long followers,
            long following
    ) {}
}
