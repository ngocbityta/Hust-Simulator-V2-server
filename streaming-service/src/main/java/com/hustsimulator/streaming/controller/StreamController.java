package com.hustsimulator.streaming.controller;

import com.hustsimulator.streaming.dto.StreamDTO;
import com.hustsimulator.streaming.service.StreamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/streams")
@RequiredArgsConstructor
@Tag(name = "Streaming API", description = "Endpoints for managing LiveKit WebRTC streams")
public class StreamController {

    private final StreamService streamService;

    @PostMapping("/start")
    @Operation(summary = "Start a stream", description = "Generate a publisher token and register stream session")
    public StreamDTO.StreamTokenResponse startStream(
            @Valid @RequestBody StreamDTO.StartStreamRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return streamService.startStream(request, userId);
    }

    @PostMapping("/{roomName}/join")
    @Operation(summary = "Join a stream", description = "Generate a subscriber token for an active stream")
    public StreamDTO.StreamTokenResponse joinStream(
            @PathVariable String roomName,
            @Valid @RequestBody StreamDTO.JoinStreamRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return streamService.joinStream(roomName, request, userId);
    }

    @PostMapping("/{roomName}/end")
    @Operation(summary = "End a stream", description = "Mark a stream session as ENDED")
    public void endStream(
            @PathVariable String roomName,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        streamService.endStream(roomName, userId);
    }

    @GetMapping("/active")
    @Operation(summary = "Get active streams", description = "List all currently active stream sessions")
    public java.util.List<StreamDTO.StreamSessionInfo> getActiveStreams() {
        return streamService.getActiveStreams();
    }

    @GetMapping("/entity/{entityType}/{entityId}")
    @Operation(summary = "Get stream for entity", description = "Find active stream for a specific event or post")
    public StreamDTO.StreamSessionInfo getStreamByEntity(
            @PathVariable String entityType,
            @PathVariable UUID entityId) {
        return streamService.getStreamByEntity(entityType, entityId);
    }

    private UUID resolveUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid X-User-Id format");
        }
    }
}
