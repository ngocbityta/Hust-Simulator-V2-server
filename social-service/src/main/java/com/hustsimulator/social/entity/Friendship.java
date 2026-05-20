package com.hustsimulator.social.entity;

import com.hustsimulator.social.common.BaseEntity;
import com.hustsimulator.social.enums.FriendshipStatus;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Bidirectional friendship: always stored with user_id < friend_id.
 * requester_id tracks who initiated the friend request.
 */
@Entity
@Table(name = "friendships",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "friend_id"}))
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Friendship extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "friend_id", nullable = false)
    private UUID friendId;

    @Column(name = "requester_id", nullable = false)
    private UUID requesterId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private FriendshipStatus status = FriendshipStatus.PENDING;
}
