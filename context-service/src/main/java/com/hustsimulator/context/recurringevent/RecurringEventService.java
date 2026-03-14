package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.entity.RecurringEvent;
import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringEventService {

    private final RecurringEventRepository recurringEventRepository;

    public List<RecurringEvent> findAll() {
        return recurringEventRepository.findAll();
    }

    public List<RecurringEvent> findActive() {
        return recurringEventRepository.findByIsActiveTrue();
    }

    public RecurringEvent findById(UUID id) {
        return recurringEventRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RecurringEvent", id));
    }

    public List<RecurringEvent> findByMapId(UUID mapId) {
        return recurringEventRepository.findByMapId(mapId);
    }

    public RecurringEvent create(RecurringEvent recurringEvent) {
        log.info("Creating recurring event: {}", recurringEvent.getName());
        return recurringEventRepository.save(recurringEvent);
    }

    public RecurringEvent update(UUID id, RecurringEvent recurringEventDetails) {
        RecurringEvent recurringEvent = findById(id);
        recurringEvent.setName(recurringEventDetails.getName());
        recurringEvent.setDescription(recurringEventDetails.getDescription());
        recurringEvent.setMapId(recurringEventDetails.getMapId());
        recurringEvent.setCronExpression(recurringEventDetails.getCronExpression());
        recurringEvent.setIsActive(recurringEventDetails.getIsActive());

        log.info("Updating recurring event: {}", id);
        return recurringEventRepository.save(recurringEvent);
    }

    public void delete(UUID id) {
        RecurringEvent recurringEvent = findById(id);
        recurringEventRepository.delete(recurringEvent);
        log.info("Deleted recurring event: {}", id);
    }
}
