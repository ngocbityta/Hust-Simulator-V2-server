package com.hustsimulator.social.friend;

import com.hustsimulator.social.entity.Friendship;
import com.hustsimulator.social.entity.UserCache;
import com.hustsimulator.social.enums.FriendshipStatus;
import com.hustsimulator.social.enums.NotificationType;
import com.hustsimulator.social.notification.NotificationService;
import com.hustsimulator.social.usercache.UserCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FriendServiceImpl implements FriendService {

    private final FriendshipRepository friendshipRepository;
    private final NotificationService notificationService;
    private final UserCacheRepository userCacheRepository;

    @Override
    @Transactional
    public FriendDTO.FriendResponse sendRequest(UUID currentUser, UUID targetUser) {
        if (currentUser.equals(targetUser)) {
            throw new IllegalArgumentException("Cannot send friend request to yourself");
        }

        // Normalize order: user_id < friend_id
        UUID smallerId = smaller(currentUser, targetUser);
        UUID largerId = larger(currentUser, targetUser);

        // Check if relationship already exists
        friendshipRepository.findByUserIdAndFriendId(smallerId, largerId)
                .ifPresent(existing -> {
                    throw new IllegalStateException(
                            "Friendship already exists with status: " + existing.getStatus());
                });

        Friendship friendship = Friendship.builder()
                .userId(smallerId)
                .friendId(largerId)
                .requesterId(currentUser)
                .status(FriendshipStatus.PENDING)
                .build();

        friendship = friendshipRepository.save(friendship);
        log.info("User {} sent friend request to user {}", currentUser, targetUser);

        // Notify the target user
        String senderName = userCacheRepository.findById(currentUser)
                .map(UserCache::getUsername)
                .orElse("Someone");
        notificationService.createNotification(
                targetUser, currentUser, NotificationType.FRIEND_REQUEST,
                friendship.getId(), senderName + " đã gửi lời mời kết bạn");

        return toFriendResponse(friendship);
    }

    @Override
    @Transactional
    public FriendDTO.FriendResponse acceptRequest(UUID currentUser, UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Friend request is not pending");
        }

        // Only the recipient (not the requester) can accept
        if (friendship.getRequesterId().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot accept your own friend request");
        }

        // Verify currentUser is part of this friendship
        if (!friendship.getUserId().equals(currentUser) && !friendship.getFriendId().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This friend request is not for you");
        }

        friendship.setStatus(FriendshipStatus.ACCEPTED);
        friendship = friendshipRepository.save(friendship);
        log.info("User {} accepted friend request from user {}", currentUser, friendship.getRequesterId());

        // Notify the requester
        String accepterName = userCacheRepository.findById(currentUser)
                .map(UserCache::getUsername)
                .orElse("Someone");
        notificationService.createNotification(
                friendship.getRequesterId(), currentUser, NotificationType.FRIEND_ACCEPTED,
                friendship.getId(), accepterName + " đã chấp nhận lời mời kết bạn");

        return toFriendResponse(friendship);
    }

    @Override
    @Transactional
    public void rejectRequest(UUID currentUser, UUID friendshipId) {
        Friendship friendship = friendshipRepository.findById(friendshipId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friend request not found"));

        if (friendship.getStatus() != FriendshipStatus.PENDING) {
            throw new IllegalStateException("Friend request is not pending");
        }

        if (friendship.getRequesterId().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Cannot reject your own friend request");
        }

        if (!friendship.getUserId().equals(currentUser) && !friendship.getFriendId().equals(currentUser)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This friend request is not for you");
        }

        friendshipRepository.delete(friendship);
        log.info("User {} rejected friend request {}", currentUser, friendshipId);
    }

    @Override
    @Transactional
    public void unfriend(UUID currentUser, UUID targetUser) {
        UUID smallerId = smaller(currentUser, targetUser);
        UUID largerId = larger(currentUser, targetUser);

        Friendship friendship = friendshipRepository.findByUserIdAndFriendId(smallerId, largerId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Friendship not found"));

        friendshipRepository.delete(friendship);
        log.info("User {} unfriended user {}", currentUser, targetUser);
    }

    @Override
    public Page<FriendDTO.FriendListResponse> getFriends(UUID userId, Pageable pageable) {
        return friendshipRepository.findAllByUserAndStatus(userId, FriendshipStatus.ACCEPTED, pageable)
                .map(f -> {
                    UUID friendId = f.getUserId().equals(userId) ? f.getFriendId() : f.getUserId();
                    UserCache friend = userCacheRepository.findById(friendId).orElse(null);
                    return new FriendDTO.FriendListResponse(
                            friendId,
                            friend != null ? friend.getUsername() : null,
                            friend != null ? friend.getAvatar() : null
                    );
                });
    }

    @Override
    public Page<FriendDTO.FriendResponse> getPendingRequests(UUID userId, Pageable pageable) {
        return friendshipRepository.findPendingRequestsForUser(userId, pageable)
                .map(this::toFriendResponse);
    }

    @Override
    public FriendshipStatus getFriendshipStatus(UUID currentUser, UUID targetUser) {
        if (currentUser.equals(targetUser)) {
            return null;
        }

        UUID smallerId = smaller(currentUser, targetUser);
        UUID largerId = larger(currentUser, targetUser);

        return friendshipRepository.findByUserIdAndFriendId(smallerId, largerId)
                .map(Friendship::getStatus)
                .orElse(null);
    }

    // --- helpers ---

    private UUID smaller(UUID a, UUID b) {
        return a.compareTo(b) < 0 ? a : b;
    }

    private UUID larger(UUID a, UUID b) {
        return a.compareTo(b) < 0 ? b : a;
    }

    private FriendDTO.FriendResponse toFriendResponse(Friendship f) {
        return new FriendDTO.FriendResponse(
                f.getId(), f.getUserId(), f.getFriendId(),
                f.getRequesterId(), f.getStatus(), f.getCreatedAt()
        );
    }
}
