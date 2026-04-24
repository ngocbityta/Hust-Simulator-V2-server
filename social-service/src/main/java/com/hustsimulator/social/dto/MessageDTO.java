package com.hustsimulator.social.dto;

import java.time.LocalDateTime;
import java.util.UUID;
import com.hustsimulator.social.enums.MessageType;

public record MessageDTO(
    UUID id,
    UUID eventId,
    UUID senderId,
    MessageType type,
    String content,
    UUID fileId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {}
