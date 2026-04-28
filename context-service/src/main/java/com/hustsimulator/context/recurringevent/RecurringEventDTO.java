package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.enums.RecurringEventStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public class RecurringEventDTO {

    public record CreateRecurringEventRequest(
            @NotBlank String name,
            String description,
            @NotNull UUID mapId,
            UUID roomId,
            @NotBlank String cronExpression,
            Integer durationMinutes
    ) {}

    public record UpdateRecurringEventRequest(
            @NotBlank String name,
            String description,
            @NotNull UUID mapId,
            UUID roomId,
            @NotBlank String cronExpression,
            @NotNull RecurringEventStatus status,
            @NotNull Integer durationMinutes
    ) {}
}
