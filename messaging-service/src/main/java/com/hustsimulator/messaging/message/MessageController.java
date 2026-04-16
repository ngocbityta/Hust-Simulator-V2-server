package com.hustsimulator.messaging.message;

import com.hustsimulator.messaging.entity.Message;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    @Data
    public static class MessageRequest {
        @NotNull
        private UUID senderId;
        @NotBlank
        private String type;
        private String content;
        private UUID fileId;
    }

    // ─── Event-scoped message endpoints ───────────────────────────────────────

    @GetMapping("/api/events/{eventId}/messages")
    public List<Message> getHistory(@PathVariable UUID eventId) {
        return messageService.getHistory(eventId);
    }

    @PostMapping("/api/events/{eventId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public Message sendMessage(
            @PathVariable UUID eventId,
            @Valid @RequestBody MessageRequest request) {
        return messageService.save(
                eventId,
                request.getSenderId(),
                request.getType(),
                request.getContent(),
                request.getFileId()
        );
    }

    // ─── Cross-service API: context-service gọi endpoint này thay vì query
    //     trực tiếp vào bảng messages_chat của messaging-service ─────────────

    /**
     * Trả về danh sách event ID mà user đã từng tham gia (đã gửi ít nhất 1 tin nhắn).
     * Endpoint này dành cho context-service gọi qua HTTP — không query cross-DB.
     */
    @GetMapping("/api/messages/participated-events")
    public List<UUID> getParticipatedEventIds(@RequestParam UUID userId) {
        return messageService.getParticipatedEventIds(userId);
    }
}
