package com.hustsimulator.context.message;

import com.hustsimulator.context.entity.Message;
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
@RequestMapping("/api/events/{eventId}/messages")
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

    @GetMapping
    public List<Message> getHistory(@PathVariable UUID eventId) {
        return messageService.getHistory(eventId);
    }

    @PostMapping
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
}
