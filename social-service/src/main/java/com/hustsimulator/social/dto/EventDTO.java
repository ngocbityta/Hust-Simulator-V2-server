package com.hustsimulator.social.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.hustsimulator.social.enums.EventType;
import com.hustsimulator.social.enums.EventStatus;

public record EventDTO(
    UUID id,
    EventType type,
    String name,
    String description,
    UUID mapId,
    EventStatus status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
