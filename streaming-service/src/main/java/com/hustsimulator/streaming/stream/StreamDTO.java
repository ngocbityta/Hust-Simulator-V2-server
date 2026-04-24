package com.hustsimulator.streaming.stream;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import com.hustsimulator.streaming.enums.StreamStatus;
import com.hustsimulator.streaming.enums.StreamEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class StreamDTO {

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StartStreamRequest {
        @NotNull(message = "Entity ID is required")
        private UUID entityId;

        @NotNull(message = "Entity Type is required (EVENT or POST)")
        private StreamEntityType entityType;

        private String participantName = "Host";
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class JoinStreamRequest {
        @NotBlank(message = "Participant Name is required")
        private String participantName;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class StreamTokenResponse {
        private String token;
        private String roomName;
        private String serverUrl;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class StreamSessionInfo {
        private UUID id;
        private String roomName;
        private StreamEntityType entityType;
        private UUID entityId;
        private StreamStatus status;
        private LocalDateTime createdAt;
    }
}
