package com.hustsimulator.social.friend;

import com.hustsimulator.social.enums.FriendshipStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FriendService {

    FriendDTO.FriendResponse sendRequest(UUID currentUser, UUID targetUser);

    FriendDTO.FriendResponse acceptRequest(UUID currentUser, UUID friendshipId);

    void rejectRequest(UUID currentUser, UUID friendshipId);

    void unfriend(UUID currentUser, UUID targetUser);

    Page<FriendDTO.FriendListResponse> getFriends(UUID userId, Pageable pageable);

    Page<FriendDTO.FriendResponse> getPendingRequests(UUID userId, Pageable pageable);

    FriendshipStatus getFriendshipStatus(UUID currentUser, UUID targetUser);
}
