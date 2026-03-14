package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "recurring_events")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecurringEvent extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "map_id", nullable = false)
    private UUID mapId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "cron_expression", nullable = false)
    private String cronExpression;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
