package com.hustsimulator.context.issue;

import com.hustsimulator.context.enums.IssueCategory;
import com.hustsimulator.context.enums.IssueStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public class FacilityIssueDTO {

    public record CreateIssueRequest(
            @NotNull UUID buildingId,
            UUID roomId,
            @NotNull UUID reporterId,
            @NotNull IssueCategory category,
            @NotBlank String description
    ) {}

    public record UpdateIssueStatusRequest(
            @NotNull IssueStatus status,
            @NotNull UUID resolvedBy
    ) {}
}
