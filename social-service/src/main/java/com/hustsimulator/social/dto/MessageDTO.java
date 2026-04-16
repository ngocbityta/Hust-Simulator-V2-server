package com.hustsimulator.social.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record MessageDTO(
    UUID id,
    UUID eventId,
    UUID senderId,
    String type,
    String content,
    UUID fileId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
