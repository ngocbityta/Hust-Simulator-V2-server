package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.entity.RecurringEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecurringEventRepository extends JpaRepository<RecurringEvent, UUID> {
    List<RecurringEvent> findByMapId(UUID mapId);
    List<RecurringEvent> findByIsActiveTrue();

    @org.springframework.data.jpa.repository.Query("SELECT r FROM RecurringEvent r WHERE r.id IN (SELECT DISTINCT m.eventId FROM Message m WHERE m.senderId = :userId)")
    List<RecurringEvent> findParticipatedEventsByUserId(@org.springframework.data.repository.query.Param("userId") UUID userId);
}
