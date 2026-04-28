package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import java.util.List;
import java.util.UUID;

public interface EventService {
    List<Event> findAll();
    List<Event> findActiveEvents();
    List<Event> findByMapId(UUID mapId);
    Event findById(UUID id);
    Event create(EventDTO.CreateEventRequest request);
    Event update(UUID id, EventDTO.UpdateEventRequest request);
    void delete(UUID id);
}
