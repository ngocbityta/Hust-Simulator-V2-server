package com.hustsimulator.context.event;

import com.hustsimulator.context.enums.EventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class EventDTO {

    public record CreateEventRequest(
            @NotBlank String name,
            String description,
            @NotNull UUID mapId,
            @NotNull LocalDateTime startTime,
            @NotNull LocalDateTime endTime,
            @NotNull com.hustsimulator.context.enums.EventType type,
            
            // INDOOR specific
            UUID buildingId,
            List<UUID> roomIds,
            
            // OUTDOOR specific
            CoordinateDTO coordinate
    ) {}

    public record UpdateEventRequest(
            @NotBlank String name,
            String description,
            @NotNull UUID mapId,
            @NotNull EventStatus status,
            @NotNull LocalDateTime startTime,
            @NotNull LocalDateTime endTime,
            
            // INDOOR specific
            UUID buildingId,
            List<UUID> roomIds,
            
            // OUTDOOR specific
            CoordinateDTO coordinate
    ) {}

    public record CoordinateDTO(
            @NotNull Double minX,
            @NotNull Double minY,
            @NotNull Double maxX,
            @NotNull Double maxY
    ) {}
}
