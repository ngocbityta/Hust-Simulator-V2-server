package com.hustsimulator.context.campus;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PlayerStateRepository extends JpaRepository<PlayerState, UUID> {

    Optional<PlayerState> findByUserId(UUID userId);

    List<PlayerState> findByActivityState(PlayerActivityState activityState);

    List<PlayerState> findByZoneId(UUID zoneId);
}
