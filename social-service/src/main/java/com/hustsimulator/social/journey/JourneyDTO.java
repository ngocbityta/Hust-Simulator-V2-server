package com.hustsimulator.social.journey;

import com.hustsimulator.social.enums.JourneyStatus;
import com.hustsimulator.social.enums.JourneyVisibility;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class JourneyDTO {

    public record LocationPoint(
            Double latitude,
            Double longitude,
            LocalDateTime timestamp
    ) {}

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
            List<LocationPoint> pathCoordinates,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) implements Serializable {}

    public record JourneyItemResponse(
            UUID id,
            UUID referenceId,
            String mediaUrl,
            String content,
            LocalDateTime timestamp,
            Integer sortOrder,
            String metadata,
            Double latitude,
            Double longitude,
            LocalDateTime startTime,
            LocalDateTime endTime,
            UUID eventId,
            List<UUID> postIds
    ) implements Serializable {}

    public record CreateJourneyRequest(
            String title,
            String description,
            LocalDate journeyDate,
            String videoUrl,
            String musicUrl,
            String templateId,
            JourneyVisibility visibility,
            List<CreateJourneyItemRequest> items,
            List<LocationPoint> pathCoordinates
    ) {}

    public record CreateJourneyItemRequest(
            UUID referenceId,
            String mediaUrl,
            String content,
            LocalDateTime timestamp,
            Integer sortOrder,
            String metadata,
            Double latitude,
            Double longitude,
            LocalDateTime startTime,
            LocalDateTime endTime,
            UUID eventId,
            List<UUID> postIds
    ) {}

    public record UpdateJourneyRequest(
            String title,
            String description,
            LocalDate journeyDate,
            String videoUrl,
            String musicUrl,
            String templateId,
            JourneyStatus status,
            JourneyVisibility visibility,
            List<CreateJourneyItemRequest> items,
            List<LocationPoint> pathCoordinates
    ) {}
}
