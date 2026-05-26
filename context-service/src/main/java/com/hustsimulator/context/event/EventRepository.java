package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

import com.hustsimulator.context.enums.EventStatus;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByStatusIn(List<EventStatus> statuses);
    List<Event> findByStatus(EventStatus status);
    List<Event> findByMapId(UUID mapId);
    
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Event e WHERE e.startTime <= :targetTime AND e.endTime >= :targetTime")
    List<Event> findActiveAt(@org.springframework.data.repository.query.Param("targetTime") java.time.LocalDateTime targetTime);

    Page<Event> findByNameContainingIgnoreCase(String name, Pageable pageable);
}
