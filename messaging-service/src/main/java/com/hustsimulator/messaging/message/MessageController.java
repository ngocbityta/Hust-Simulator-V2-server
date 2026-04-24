package com.hustsimulator.messaging.message;

import com.hustsimulator.messaging.entity.Message;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import com.hustsimulator.messaging.enums.MessageType;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@Tag(name = "Messages", description = "Chat messages scoped to recurring events")
public class MessageController {

    private final MessageService messageService;

    @Data
    public static class MessageRequest {
        @NotNull
        private MessageType type;
        private String content;
        private UUID fileId;
    }

    // ─── Primary endpoints (recurring-events scope) ────────────────────────────

    @Operation(summary = "Get message history for a recurring event")
    @GetMapping("/api/recurring-events/{recurringEventId}/messages")
    public List<Message> getHistory(@PathVariable UUID recurringEventId) {
        return messageService.getHistory(recurringEventId);
    }

    @Operation(summary = "Send a message to a recurring event chat")
    @PostMapping("/api/recurring-events/{recurringEventId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public Message sendMessage(
            @PathVariable UUID recurringEventId,
            @Valid @RequestBody MessageRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {

        UUID senderId = resolveUserId(userIdHeader);
        return messageService.save(
                recurringEventId,
                senderId,
                request.getType(),
                request.getContent(),
                request.getFileId());
    }

    // ─── User-scoped endpoints ─────────────────────────────────────────────────

    @Operation(summary = "Get IDs of recurring events the current user has participated in")
    @GetMapping("/api/messages/participated-events")
    public List<UUID> getParticipatedEventIds(
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return messageService.getParticipatedEventIds(userId);
    }

    // ─── Helpers ───────────────────────────────────────────────────────────────

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
