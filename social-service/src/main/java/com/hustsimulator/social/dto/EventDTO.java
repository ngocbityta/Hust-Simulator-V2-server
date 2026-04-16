package com.hustsimulator.social.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record EventDTO(
    UUID id,
    String type,
    String name,
    String description,
    UUID mapId,
    String status,
    LocalDateTime startTime,
    LocalDateTime endTime,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
