package com.hustsimulator.context.recurringeventdetail;

import com.hustsimulator.context.entity.RecurringEventDetail;
import com.hustsimulator.context.enums.RecurringEventDetailStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RecurringEventDetailRepository extends JpaRepository<RecurringEventDetail, UUID> {

    List<RecurringEventDetail> findByRecurringEventIdOrderByScheduledAtDesc(UUID recurringEventId);

    Optional<RecurringEventDetail> findByRecurringEventIdAndScheduledAt(UUID recurringEventId, LocalDateTime scheduledAt);

    Optional<RecurringEventDetail> findFirstByRecurringEventIdAndStatus(UUID recurringEventId, RecurringEventDetailStatus status);
}
