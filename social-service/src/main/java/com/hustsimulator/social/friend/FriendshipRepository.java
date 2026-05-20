package com.hustsimulator.social.friend;

import com.hustsimulator.social.entity.Friendship;
import com.hustsimulator.social.enums.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FriendshipRepository extends JpaRepository<Friendship, UUID> {

    Optional<Friendship> findByUserIdAndFriendId(UUID userId, UUID friendId);

    /**
     * Find all ACCEPTED friendships for a user (appears as either userId or friendId).
     */
    @Query("SELECT f FROM Friendship f WHERE (f.userId = :uid OR f.friendId = :uid) AND f.status = :status")
    Page<Friendship> findAllByUserAndStatus(@Param("uid") UUID userId,
                                            @Param("status") FriendshipStatus status,
                                            Pageable pageable);

    /**
     * Find pending friend requests where the user is the recipient (not the requester).
     */
    @Query("SELECT f FROM Friendship f WHERE (f.userId = :uid OR f.friendId = :uid) " +
           "AND f.status = 'PENDING' AND f.requesterId <> :uid")
    Page<Friendship> findPendingRequestsForUser(@Param("uid") UUID userId, Pageable pageable);

    /**
     * Check if any friendship exists between two users (regardless of order).
     */
    @Query("SELECT COUNT(f) > 0 FROM Friendship f WHERE f.userId = :uid1 AND f.friendId = :uid2")
    boolean existsBetween(@Param("uid1") UUID smallerId, @Param("uid2") UUID largerId);
}
