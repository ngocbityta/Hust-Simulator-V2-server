package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.entity.RecurringEvent;
import java.util.List;
import java.util.UUID;

public interface RecurringEventService {
    List<RecurringEvent> findAll();
    List<RecurringEvent> findActive();
    List<RecurringEvent> findScheduled();
    RecurringEvent findById(UUID id);
    List<RecurringEvent> findByMapId(UUID mapId);
    List<RecurringEvent> findParticipatedEventsByUserId(UUID userId);
    void activateClass(UUID classId);
    void completeClass(UUID classId);
    RecurringEvent create(RecurringEventDTO.CreateRecurringEventRequest request);
    RecurringEvent update(UUID id, RecurringEventDTO.UpdateRecurringEventRequest request);
    void delete(UUID id);
}
