package com.hustsimulator.social.follow;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FollowService {

    void follow(UUID currentUser, UUID targetUser);

    void unfollow(UUID currentUser, UUID targetUser);

    Page<FollowDTO.FollowResponse> getFollowers(UUID userId, Pageable pageable);

    Page<FollowDTO.FollowResponse> getFollowing(UUID userId, Pageable pageable);

    FollowDTO.FollowCountResponse getFollowCounts(UUID userId);

    boolean isFollowing(UUID currentUser, UUID targetUser);
}
