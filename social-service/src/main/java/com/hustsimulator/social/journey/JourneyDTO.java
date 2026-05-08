package com.hustsimulator.social.journey;

import com.hustsimulator.social.enums.JourneyItemType;
import com.hustsimulator.social.enums.JourneyStatus;
import com.hustsimulator.social.enums.JourneyVisibility;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class JourneyDTO {

    public record JourneyResponse(
            UUID id,
            UUID userId,
            String title,
            String description,
            LocalDate journeyDate,
            String videoUrl,
            String musicUrl,
            String templateId,
            JourneyStatus status,
            JourneyVisibility visibility,
            List<JourneyItemResponse> items,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    public record JourneyItemResponse(
            UUID id,
            JourneyItemType type,
            UUID referenceId,
            String mediaUrl,
            String content,
            LocalDateTime timestamp,
            Integer sortOrder,
            String metadata
    ) {}

    public record CreateJourneyRequest(
            String title,
            String description,
            LocalDate journeyDate,
            String musicUrl,
            String templateId,
            JourneyVisibility visibility,
            List<CreateJourneyItemRequest> items
    ) {}

    public record CreateJourneyItemRequest(
            JourneyItemType type,
            UUID referenceId,
            String mediaUrl,
            String content,
            LocalDateTime timestamp,
            Integer sortOrder,
            String metadata
    ) {}

    public record UpdateJourneyRequest(
            String title,
            String description,
            String musicUrl,
            String templateId,
            JourneyVisibility visibility,
            List<CreateJourneyItemRequest> items
    ) {}
}
