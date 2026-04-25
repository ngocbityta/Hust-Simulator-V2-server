package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import com.hustsimulator.context.enums.RecurringEventDetailStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Represents a single concrete occurrence of a {@link RecurringEvent}.
 * <p>
 * Example: A recurring event scheduled every Tuesday produces one
 * {@code RecurringEventDetail} per week.  Messages are scoped to a detail,
 * not to the parent recurring event, so chat history is isolated per occurrence.
 */
@Entity
@Table(
    name = "recurring_event_details",
    uniqueConstraints = @UniqueConstraint(columnNames = {"recurring_event_id", "scheduled_at"})
)
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringEventDetail extends BaseEntity {

    @Column(name = "recurring_event_id", nullable = false)
    private UUID recurringEventId;

    /** Exact start time of this occurrence (derived from the cron expression). */
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    /** Filled in when END_CLASS fires. */
    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RecurringEventDetailStatus status = RecurringEventDetailStatus.SCHEDULED;
}
