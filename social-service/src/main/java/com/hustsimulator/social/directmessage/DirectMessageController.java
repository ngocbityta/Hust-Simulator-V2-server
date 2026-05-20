package com.hustsimulator.social.directmessage;

import com.hustsimulator.social.directmessage.dto.ConversationDto;
import com.hustsimulator.social.directmessage.dto.DirectMessageDto;
import com.hustsimulator.social.directmessage.dto.SendDirectMessageRequest;
import com.hustsimulator.social.directmessage.service.DirectMessageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/messages/direct")
@RequiredArgsConstructor
@Tag(name = "Direct Messages", description = "1-1 Chat APIs")
public class DirectMessageController {

    private final DirectMessageService directMessageService;

    @Operation(summary = "Get user's conversations list")
    @GetMapping("/conversations")
    public List<ConversationDto> getUserConversations(@RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return directMessageService.getUserConversations(userId);
    }

    @Operation(summary = "Get chat history with a partner")
    @GetMapping("/{partnerId}")
    public Page<DirectMessageDto> getHistory(
            @RequestHeader("X-User-Id") String userIdHeader,
            @PathVariable UUID partnerId,
            Pageable pageable) {
        UUID userId = resolveUserId(userIdHeader);
        return directMessageService.getConversationHistory(userId, partnerId, pageable);
    }

    @Operation(summary = "Send a direct message")
    @PostMapping("/{partnerId}")
    @ResponseStatus(HttpStatus.CREATED)
    public DirectMessageDto sendMessage(
            @RequestHeader("X-User-Id") String userIdHeader,
            @PathVariable UUID partnerId,
            @Valid @RequestBody SendDirectMessageRequest request) {
        UUID senderId = resolveUserId(userIdHeader);
        return directMessageService.sendMessage(senderId, partnerId, request.getContent());
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
