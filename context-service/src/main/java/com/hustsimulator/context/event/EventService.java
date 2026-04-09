package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import java.util.List;
import java.util.UUID;

public interface EventService {
    List<Event> findAll();
    List<Event> findActiveEvents();
    List<Event> findByMapId(UUID mapId);
    Event findById(UUID id);
    Event create(Event event);
    Event update(UUID id, Event eventDetails);
    void delete(UUID id);
}
