package com.hustsimulator.context.userstate;

import com.hustsimulator.context.entity.EventAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EventAttendanceRepository extends JpaRepository<EventAttendance, UUID> {
    Optional<EventAttendance> findTopByUserIdAndEventIdAndLeftAtIsNullOrderByJoinedAtDesc(UUID userId, UUID eventId);

    @org.springframework.data.jpa.repository.Query("SELECT COUNT(DISTINCT a.userId) FROM EventAttendance a WHERE a.eventId = :eventId")
    long countDistinctUserIdByEventId(@org.springframework.data.repository.query.Param("eventId") UUID eventId);
}
