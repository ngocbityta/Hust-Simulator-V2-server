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

    @org.springframework.data.jpa.repository.Query("SELECT r.status, COUNT(r) FROM RecurringEventDetail r WHERE r.scheduledAt >= :startOfDay AND r.scheduledAt < :endOfDay GROUP BY r.status")
    List<Object[]> countStatusByScheduledAtBetween(@org.springframework.data.repository.query.Param("startOfDay") LocalDateTime startOfDay, @org.springframework.data.repository.query.Param("endOfDay") LocalDateTime endOfDay);
}
