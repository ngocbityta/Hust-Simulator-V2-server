package com.hustsimulator.social.follow;

import com.hustsimulator.social.entity.Follow;
import com.hustsimulator.social.entity.UserCache;
import com.hustsimulator.social.enums.NotificationType;
import com.hustsimulator.social.notification.NotificationService;
import com.hustsimulator.social.usercache.UserCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FollowServiceImpl implements FollowService {

    private final FollowRepository followRepository;
    private final NotificationService notificationService;
    private final UserCacheRepository userCacheRepository;

    @Override
    @Transactional
    public void follow(UUID currentUser, UUID targetUser) {
        if (currentUser.equals(targetUser)) {
            throw new IllegalArgumentException("Cannot follow yourself");
        }

        if (followRepository.existsByFollowerIdAndFollowingId(currentUser, targetUser)) {
            log.info("User {} already follows user {}", currentUser, targetUser);
            return; // idempotent
        }

        Follow follow = Follow.builder()
                .followerId(currentUser)
                .followingId(targetUser)
                .build();
        followRepository.save(follow);
        log.info("User {} followed user {}", currentUser, targetUser);

        // Notify the followed user
        String followerName = userCacheRepository.findById(currentUser)
                .map(UserCache::getUsername)
                .orElse("Someone");
        notificationService.createNotification(
                targetUser, currentUser, NotificationType.FOLLOW,
                null, followerName + " đã follow bạn");
    }

    @Override
    @Transactional
    public void unfollow(UUID currentUser, UUID targetUser) {
        followRepository.findByFollowerIdAndFollowingId(currentUser, targetUser)
                .ifPresent(follow -> {
                    followRepository.delete(follow);
                    log.info("User {} unfollowed user {}", currentUser, targetUser);
                });
    }

    @Override
    public Page<FollowDTO.FollowResponse> getFollowers(UUID userId, Pageable pageable) {
        return followRepository.findByFollowingIdOrderByCreatedAtDesc(userId, pageable)
                .map(follow -> toFollowResponse(follow.getFollowerId(), userId));
    }

    @Override
    public Page<FollowDTO.FollowResponse> getFollowing(UUID userId, Pageable pageable) {
        return followRepository.findByFollowerIdOrderByCreatedAtDesc(userId, pageable)
                .map(follow -> toFollowResponse(follow.getFollowingId(), userId));
    }

    @Override
    public FollowDTO.FollowCountResponse getFollowCounts(UUID userId) {
        long followers = followRepository.countByFollowingId(userId);
        long following = followRepository.countByFollowerId(userId);
        return new FollowDTO.FollowCountResponse(followers, following);
    }

    @Override
    public boolean isFollowing(UUID currentUser, UUID targetUser) {
        return followRepository.existsByFollowerIdAndFollowingId(currentUser, targetUser);
    }

    private FollowDTO.FollowResponse toFollowResponse(UUID targetUserId, UUID currentUserId) {
        UserCache user = userCacheRepository.findById(targetUserId).orElse(null);
        boolean isFollowing = followRepository.existsByFollowerIdAndFollowingId(currentUserId, targetUserId);
        return new FollowDTO.FollowResponse(
                targetUserId,
                user != null ? user.getUsername() : null,
                user != null ? user.getAvatar() : null,
                isFollowing
        );
    }
}
