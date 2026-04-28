package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.entity.RecurringEvent;
import com.hustsimulator.context.entity.Room;
import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.enums.RecurringEventStatus;
import com.hustsimulator.context.enums.RoomStatus;
import com.hustsimulator.context.messaging.MessagingServiceClient;
import com.hustsimulator.context.room.RoomRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringEventServiceImpl implements RecurringEventService {

    private final RecurringEventRepository recurringEventRepository;
    private final RoomRepository roomRepository;
    private final MessagingServiceClient messagingServiceClient;
    private final com.hustsimulator.context.recurringeventdetail.RecurringEventDetailRepository recurringEventDetailRepository;

    @Override
    public List<RecurringEvent> findAll() {
        return recurringEventRepository.findAll();
    }

    @Override
    public List<RecurringEvent> findActive() {
        return recurringEventRepository.findByStatus(RecurringEventStatus.ONGOING);
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
        // Gọi messaging-service qua HTTP thay vì query cross-service DB trực tiếp
        // Note: messaging-service trả về danh sách RecurringEventDetail IDs
        List<UUID> participatedDetailIds = messagingServiceClient.getParticipatedEventIds(userId);
        if (participatedDetailIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // Map detailIds sang recurringEventIds
        List<UUID> recurringEventIds = recurringEventDetailRepository.findAllById(participatedDetailIds)
                .stream()
                .map(com.hustsimulator.context.entity.RecurringEventDetail::getRecurringEventId)
                .distinct()
                .toList();

        if (recurringEventIds.isEmpty()) {
            return Collections.emptyList();
        }

        return recurringEventRepository.findByIdIn(recurringEventIds);
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
        
        event.setStatus(RecurringEventStatus.SCHEDULED);
        recurringEventRepository.save(event);

        if (event.getRoomId() != null) {
            Room room = roomRepository.findById(event.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", event.getRoomId()));
            room.setStatus(RoomStatus.EMPTY);
            roomRepository.save(room);
        }
    }

    @Override
    public RecurringEvent create(RecurringEventDTO.CreateRecurringEventRequest request) {
        log.info("Creating recurring event: {}", request.name());
        RecurringEvent recurringEvent = RecurringEvent.builder()
                .name(request.name())
                .description(request.description())
                .mapId(request.mapId())
                .roomId(request.roomId())
                .cronExpression(request.cronExpression())
                .durationMinutes(request.durationMinutes() != null ? request.durationMinutes() : 60)
                .status(RecurringEventStatus.SCHEDULED)
                .build();
        return recurringEventRepository.save(recurringEvent);
    }

    @Override
    @Transactional
    public RecurringEvent update(UUID id, RecurringEventDTO.UpdateRecurringEventRequest request) {
        RecurringEvent recurringEvent = findById(id);
        RecurringEventStatus oldStatus = recurringEvent.getStatus();

        recurringEvent.setName(request.name());
        recurringEvent.setDescription(request.description());
        recurringEvent.setMapId(request.mapId());
        if (request.roomId() != null) {
            recurringEvent.setRoomId(request.roomId());
        }
        recurringEvent.setCronExpression(request.cronExpression());
        recurringEvent.setDurationMinutes(request.durationMinutes());
        
        if (request.status() != null) {
            recurringEvent.setStatus(request.status());
        }

        log.info("Updating recurring event: {}", id);
        final RecurringEvent savedEvent = recurringEventRepository.save(recurringEvent);

        if (oldStatus != savedEvent.getStatus() && savedEvent.getRoomId() != null) {
            roomRepository.findById(savedEvent.getRoomId()).ifPresent(room -> {
                if (savedEvent.getStatus() == RecurringEventStatus.ONGOING) {
                    room.setStatus(RoomStatus.BUSY);
                } else {
                    room.setStatus(RoomStatus.EMPTY);
                }
                roomRepository.save(room);
            });
        }

        return savedEvent;
    }

    @Override
    public void delete(UUID id) {
        RecurringEvent recurringEvent = findById(id);
        recurringEventRepository.delete(recurringEvent);
        log.info("Deleted recurring event: {}", id);
    }
}
