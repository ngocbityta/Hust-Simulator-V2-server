package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.entity.RecurringEvent;
import com.hustsimulator.context.entity.Room;
import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.enums.RecurringEventStatus;
import com.hustsimulator.context.enums.RoomStatus;
import com.hustsimulator.context.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringEventServiceImpl implements RecurringEventService {

    private final RecurringEventRepository recurringEventRepository;
    private final RoomRepository roomRepository;

    @Override
    public List<RecurringEvent> findAll() {
        return recurringEventRepository.findAll();
    }

    @Override
    public List<RecurringEvent> findActive() {
        return recurringEventRepository.findByStatusIn(List.of(RecurringEventStatus.SCHEDULED, RecurringEventStatus.ONGOING));
    }

    @Override
    public List<RecurringEvent> findScheduled() {
        return recurringEventRepository.findByStatus(RecurringEventStatus.SCHEDULED);
    }

    @Override
    public RecurringEvent findById(UUID id) {
        return recurringEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringEvent", id));
    }

    @Override
    public List<RecurringEvent> findByMapId(UUID mapId) {
        return recurringEventRepository.findByMapId(mapId);
    }

    @Override
    public List<RecurringEvent> findParticipatedEventsByUserId(UUID userId) {
        return recurringEventRepository.findParticipatedEventsByUserId(userId);
    }

    @Override
    @Transactional
    public void activateClass(UUID classId) {
        RecurringEvent event = findById(classId);
        log.info("Activating class: {}", event.getName());
        
        event.setStatus(RecurringEventStatus.ONGOING);
        recurringEventRepository.save(event);

        if (event.getRoomId() != null) {
            Room room = roomRepository.findById(event.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", event.getRoomId()));
            room.setStatus(RoomStatus.BUSY);
            roomRepository.save(room);
        }
    }

    @Override
    @Transactional
    public void completeClass(UUID classId) {
        RecurringEvent event = findById(classId);
        log.info("Completing class: {}", event.getName());
        
        event.setStatus(RecurringEventStatus.COMPLETED);
        recurringEventRepository.save(event);

        if (event.getRoomId() != null) {
            Room room = roomRepository.findById(event.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", event.getRoomId()));
            room.setStatus(RoomStatus.EMPTY);
            roomRepository.save(room);
        }
    }

    @Override
    public RecurringEvent create(RecurringEvent recurringEvent) {
        log.info("Creating recurring event: {}", recurringEvent.getName());
        return recurringEventRepository.save(recurringEvent);
    }

    @Override
    public RecurringEvent update(UUID id, RecurringEvent recurringEventDetails) {
        RecurringEvent recurringEvent = findById(id);
        recurringEvent.setName(recurringEventDetails.getName());
        recurringEvent.setDescription(recurringEventDetails.getDescription());
        recurringEvent.setMapId(recurringEventDetails.getMapId());
        recurringEvent.setCronExpression(recurringEventDetails.getCronExpression());
        recurringEvent.setDurationMinutes(recurringEventDetails.getDurationMinutes());

        log.info("Updating recurring event: {}", id);
        return recurringEventRepository.save(recurringEvent);
    }

    @Override
    public void delete(UUID id) {
        RecurringEvent recurringEvent = findById(id);
        recurringEventRepository.delete(recurringEvent);
        log.info("Deleted recurring event: {}", id);
    }
}
