package com.hustsimulator.context.chat;

import com.hustsimulator.context.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    // --- Conversations ---

    @GetMapping("/conversations/user/{userId}")
    public List<Conversation> findConversations(@PathVariable UUID userId) {
        return chatService.findConversationsByUserId(userId);
    }

    @GetMapping("/conversations/{id}")
    public Conversation findConversationById(@PathVariable UUID id) {
        return chatService.findConversationById(id);
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public Conversation getOrCreate(@RequestParam UUID partnerAId, @RequestParam UUID partnerBId) {
        return chatService.getOrCreateConversation(partnerAId, partnerBId);
    }

    @DeleteMapping("/conversations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteConversation(@PathVariable UUID id) {
        chatService.deleteConversation(id);
    }

    // --- Messages ---

    @GetMapping("/messages/conversation/{conversationId}")
    public List<Message> findMessages(@PathVariable UUID conversationId) {
        return chatService.findMessagesByConversationId(conversationId);
    }

    @PostMapping("/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public Message sendMessage(@Valid @RequestBody Message message) {
        return chatService.sendMessage(message);
    }

    @DeleteMapping("/messages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable UUID id) {
        chatService.deleteMessage(id);
    }
}
