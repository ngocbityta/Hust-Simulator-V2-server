package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.entity.RecurringEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RecurringEventRepository extends JpaRepository<RecurringEvent, UUID> {
    List<RecurringEvent> findByMapId(UUID mapId);
    List<RecurringEvent> findByIsActiveTrue();
}
