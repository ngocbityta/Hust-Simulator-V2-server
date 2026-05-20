package com.hustsimulator.social.friend;

import com.hustsimulator.social.enums.FriendshipStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Objects for Friend operations.
 */
public class FriendDTO {

    public record SendFriendRequest(UUID targetUserId) {}

    public record FriendResponse(
            UUID friendshipId,
            UUID userId,
            UUID friendId,
            UUID requesterId,
            FriendshipStatus status,
            LocalDateTime createdAt
    ) {}

    public record FriendListResponse(
            UUID friendId,
            String username,
            String avatar
    ) {}
}
