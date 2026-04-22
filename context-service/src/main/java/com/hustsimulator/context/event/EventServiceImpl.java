package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hustsimulator.context.building.BuildingRepository;
import com.hustsimulator.context.common.GeometryUtils;
import com.hustsimulator.context.entity.Building;
import com.hustsimulator.context.entity.IndoorEvent;
import com.hustsimulator.context.entity.OutdoorEvent;
import com.hustsimulator.context.enums.EventStatus;
import org.locationtech.jts.geom.Polygon;

import java.util.List;
import java.util.UUID;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class EventServiceImpl implements EventService {

    private final EventRepository eventRepository;
    private final BuildingRepository buildingRepository;
    private final ObjectMapper objectMapper;
    private final EventEventPublisher eventEventPublisher;

    @Override
    public List<Event> findAll() {
        return eventRepository.findAll();
    }

    @Override
    public List<Event> findActiveEvents() {
        return eventRepository.findByStatus(EventStatus.ONGOING);
    }

    @Override
    public List<Event> findByMapId(UUID mapId) {
        return eventRepository.findByMapId(mapId);
    }

    @Override
    public Event findById(UUID id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Event", id));
    }

    @Override
    public Event create(Event event) {
        log.info("Creating new event: {}", event.getName());
        validateEvent(event);
        Event saved = eventRepository.save(event);
        eventEventPublisher.publish(saved, EventEvent.EventType.CREATED);
        return saved;
    }

    @Override
    public Event update(UUID id, Event eventDetails) {
        Event event = findById(id);
        
        if (event.getClass() != eventDetails.getClass()) {
            throw new IllegalArgumentException("Cannot change event type");
        }
        
        event.setName(eventDetails.getName());
        event.setDescription(eventDetails.getDescription());
        event.setMapId(eventDetails.getMapId());
        event.setStatus(eventDetails.getStatus());
        event.setStartTime(eventDetails.getStartTime());
        event.setEndTime(eventDetails.getEndTime());

        if (event instanceof IndoorEvent indoorEvent && eventDetails instanceof IndoorEvent indoorDetails) {
            indoorEvent.setBuildingId(indoorDetails.getBuildingId());
            indoorEvent.setRoomIds(indoorDetails.getRoomIds());
        } else if (event instanceof OutdoorEvent outdoorEvent && eventDetails instanceof OutdoorEvent outdoorDetails) {
            outdoorEvent.setMinX(outdoorDetails.getMinX());
            outdoorEvent.setMinY(outdoorDetails.getMinY());
            outdoorEvent.setMaxX(outdoorDetails.getMaxX());
            outdoorEvent.setMaxY(outdoorDetails.getMaxY());
        }

        validateEvent(event);

        log.info("Updating event: {}", id);
        Event saved = eventRepository.save(event);
        eventEventPublisher.publish(saved, EventEvent.EventType.UPDATED);
        return saved;
    }

    private void validateEvent(Event event) {
        if (event instanceof OutdoorEvent outdoor) {
            if (outdoor.getMinX() == null || outdoor.getMinY() == null || outdoor.getMaxX() == null || outdoor.getMaxY() == null) {
                throw new IllegalArgumentException("Outdoor event must have bounding box coordinates");
            }
            if (outdoor.getMinX() >= outdoor.getMaxX() || outdoor.getMinY() >= outdoor.getMaxY()) {
                throw new IllegalArgumentException("Outdoor event bounding box is invalid: min coordinates must be strictly less than max coordinates");
            }
            List<Building> buildings = buildingRepository.findByMapId(event.getMapId());
            
            // Create event polygon from bounding box
            List<double[]> eventPoints = new ArrayList<>();
            eventPoints.add(new double[]{outdoor.getMinX(), outdoor.getMinY()});
            eventPoints.add(new double[]{outdoor.getMaxX(), outdoor.getMinY()});
            eventPoints.add(new double[]{outdoor.getMaxX(), outdoor.getMaxY()});
            eventPoints.add(new double[]{outdoor.getMinX(), outdoor.getMaxY()});
            eventPoints.add(new double[]{outdoor.getMinX(), outdoor.getMinY()});
            
            Polygon eventPolygon = GeometryUtils.createPolygon(eventPoints);
            
            for (Building building : buildings) {
                Polygon buildingPolygon = GeometryUtils.createPolygonFromJson(building.getCoordinates(), objectMapper);
                if (eventPolygon.intersects(buildingPolygon)) {
                    throw new IllegalArgumentException("Outdoor event overlaps with building: " + building.getName());
                }
            }
        }
    }

    @Override
    public void delete(UUID id) {
        Event event = findById(id);
        eventRepository.delete(event);
        eventEventPublisher.publish(event, EventEvent.EventType.DELETED);
        log.info("Deleted event: {}", id);
    }
}
