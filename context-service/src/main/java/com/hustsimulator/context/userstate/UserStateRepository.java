package com.hustsimulator.context.userstate;

import com.hustsimulator.context.enums.UserActivityState;
import com.hustsimulator.context.entity.UserState;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserStateRepository extends JpaRepository<UserState, UUID> {
    Optional<UserState> findByUserId(UUID userId);
    List<UserState> findByActivityState(UserActivityState state);
    List<UserState> findByMapId(UUID mapId);
    List<UserState> findByEventId(UUID eventId);
}
