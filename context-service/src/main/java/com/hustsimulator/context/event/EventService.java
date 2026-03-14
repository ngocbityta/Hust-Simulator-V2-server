package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventService {

    private final EventRepository eventRepository;

    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    public List<Event> findActiveEvents() {
        return eventRepository.findByIsActiveTrue();
    }

    public List<Event> findByMapId(UUID mapId) {
        return eventRepository.findByMapId(mapId);
    }

    public Event findById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    public Event create(Event event) {
        log.info("Creating new event: {}", event.getName());
        return eventRepository.save(event);
    }

    public Event update(UUID id, Event eventDetails) {
        Event event = findById(id);
        event.setName(eventDetails.getName());
        event.setDescription(eventDetails.getDescription());
        event.setMapId(eventDetails.getMapId());
        event.setRoomId(eventDetails.getRoomId());
        event.setStartTime(eventDetails.getStartTime());
        event.setEndTime(eventDetails.getEndTime());
        event.setIsActive(eventDetails.getIsActive());

        log.info("Updating event: {}", id);
        return eventRepository.save(event);
    }

    public void delete(UUID id) {
        Event event = findById(id);
        eventRepository.delete(event);
        log.info("Deleted event: {}", id);
    }
}
