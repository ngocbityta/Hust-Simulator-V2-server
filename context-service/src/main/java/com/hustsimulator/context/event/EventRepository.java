package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

import com.hustsimulator.context.enums.EventStatus;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByStatusIn(List<EventStatus> statuses);
    List<Event> findByStatus(EventStatus status);
    List<Event> findByMapId(UUID mapId);
}
