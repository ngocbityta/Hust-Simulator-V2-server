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
    public Event create(EventDTO.CreateEventRequest request) {
        log.info("Creating new event: {}", request.name());
        Event event = mapToEntity(request);
        validateEvent(event);
        Event saved = eventRepository.save(event);
        eventEventPublisher.publish(saved, EventEvent.EventType.CREATED);
        return saved;
    }

    private Event mapToEntity(EventDTO.CreateEventRequest request) {
        Event event;
        if (com.hustsimulator.context.enums.EventType.INDOOR.equals(request.type())) {
            IndoorEvent indoorEvent = new IndoorEvent();
            indoorEvent.setBuildingId(request.buildingId());
            indoorEvent.setRoomIds(request.roomIds());
            event = indoorEvent;
        } else if (com.hustsimulator.context.enums.EventType.OUTDOOR.equals(request.type())) {
            OutdoorEvent outdoorEvent = new OutdoorEvent();
            if (request.coordinate() != null) {
                outdoorEvent.setMinX(request.coordinate().minX());
                outdoorEvent.setMinY(request.coordinate().minY());
                outdoorEvent.setMaxX(request.coordinate().maxX());
                outdoorEvent.setMaxY(request.coordinate().maxY());
            }
            event = outdoorEvent;
        } else {
            throw new IllegalArgumentException("Invalid event type: " + request.type());
        }

        event.setName(request.name());
        event.setDescription(request.description());
        event.setMapId(request.mapId());
        event.setStatus(EventStatus.SCHEDULED);
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());

        return event;
    }

    @Override
    public Event update(UUID id, EventDTO.UpdateEventRequest request) {
        Event event = findById(id);
        
        event.setName(request.name());
        event.setDescription(request.description());
        event.setMapId(request.mapId());
        event.setStatus(request.status());
        event.setStartTime(request.startTime());
        event.setEndTime(request.endTime());

        if (event instanceof IndoorEvent indoorEvent) {
            indoorEvent.setBuildingId(request.buildingId());
            indoorEvent.setRoomIds(request.roomIds());
        } else if (event instanceof OutdoorEvent outdoorEvent) {
            if (request.coordinate() != null) {
                outdoorEvent.setMinX(request.coordinate().minX());
                outdoorEvent.setMinY(request.coordinate().minY());
                outdoorEvent.setMaxX(request.coordinate().maxX());
                outdoorEvent.setMaxY(request.coordinate().maxY());
            }
        }

        validateEvent(event);

        log.info("Updating event: {}", id);
        Event saved = eventRepository.save(event);
        eventEventPublisher.publish(saved, EventEvent.EventType.UPDATED);
        return saved;
    }

    private void validateEvent(Event event) {
        if (event instanceof OutdoorEvent outdoor) {
            validateOutdoorCoordinates(outdoor);
            validateOutdoorEventOverlap(outdoor);
        }
    }

    private void validateOutdoorCoordinates(OutdoorEvent outdoor) {
        if (outdoor.getMinX() == null || outdoor.getMinY() == null || outdoor.getMaxX() == null || outdoor.getMaxY() == null) {
            throw new IllegalArgumentException("Outdoor event must have bounding box coordinates");
        }
        if (outdoor.getMinX() >= outdoor.getMaxX() || outdoor.getMinY() >= outdoor.getMaxY()) {
            throw new IllegalArgumentException("Outdoor event bounding box is invalid: min coordinates must be strictly less than max coordinates");
        }
    }

    private void validateOutdoorEventOverlap(OutdoorEvent outdoor) {
        List<Building> buildings = buildingRepository.findByMapId(outdoor.getMapId());
        
        List<double[]> eventPoints = List.of(
                new double[]{outdoor.getMinX(), outdoor.getMinY()},
                new double[]{outdoor.getMaxX(), outdoor.getMinY()},
                new double[]{outdoor.getMaxX(), outdoor.getMaxY()},
                new double[]{outdoor.getMinX(), outdoor.getMaxY()},
                new double[]{outdoor.getMinX(), outdoor.getMinY()}
        );
        
        Polygon eventPolygon = GeometryUtils.createPolygon(eventPoints);
        
        for (Building building : buildings) {
            Polygon buildingPolygon = GeometryUtils.createPolygonFromJson(building.getCoordinates(), objectMapper);
            if (eventPolygon.intersects(buildingPolygon)) {
                throw new IllegalArgumentException("Outdoor event overlaps with building: " + building.getName());
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
