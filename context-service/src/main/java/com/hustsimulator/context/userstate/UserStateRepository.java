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

    @org.springframework.data.jpa.repository.Query("SELECT us.activityState, COUNT(us) FROM UserState us GROUP BY us.activityState")
    List<Object[]> countByActivityState();

    @org.springframework.data.jpa.repository.Query("SELECT us.buildingId, COUNT(us) FROM UserState us WHERE us.buildingId IS NOT NULL GROUP BY us.buildingId")
    List<Object[]> countUsersByBuilding();

    @org.springframework.data.jpa.repository.Query("SELECT us.eventId, COUNT(us) FROM UserState us WHERE us.eventId IN :eventIds GROUP BY us.eventId")
    List<Object[]> countUsersByEventIds(@org.springframework.data.repository.query.Param("eventIds") List<UUID> eventIds);
}
