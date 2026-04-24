package com.hustsimulator.social.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.hustsimulator.social.enums.RecurringEventStatus;

public record RecurringEventDTO(
    UUID id,
    String name,
    String description,
    UUID mapId,
    UUID roomId,
    String cronExpression,
    RecurringEventStatus status,
    Integer durationMinutes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
