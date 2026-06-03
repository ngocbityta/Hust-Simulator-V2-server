package com.hustsimulator.social.location;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

public record LocationDTO() {

    @Schema(description = "A single location point with timestamp")
    public record LocationPoint(
            @Schema(description = "Latitude coordinate", example = "21.0071")
            Double latitude,
            @Schema(description = "Longitude coordinate", example = "105.8431")
            Double longitude,
            @Schema(description = "Timestamp when location was recorded", example = "2026-06-03T23:30:00")
            LocalDateTime timestamp
    ) {}

    @Schema(description = "Request body for batch uploading user locations (called every 10s)")
    public record SaveLocationsRequest(
            @Schema(description = "List of locations collected in the last interval")
            List<LocationPoint> points
    ) {}
}
