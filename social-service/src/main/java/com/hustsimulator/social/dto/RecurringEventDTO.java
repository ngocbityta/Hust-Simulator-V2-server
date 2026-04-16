package com.hustsimulator.social.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record RecurringEventDTO(
    UUID id,
    String name,
    String description,
    UUID mapId,
    UUID roomId,
    String cronExpression,
    String status,
    Integer durationMinutes,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
