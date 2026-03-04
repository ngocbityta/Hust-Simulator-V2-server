package com.hustsimulator.context.chat;

import com.hustsimulator.context.entity.*;

import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    // --- Conversations ---

    public List<Conversation> findConversationsByUserId(UUID userId) {
        return conversationRepository.findByUserId(userId);
    }

    public Conversation findConversationById(UUID id) {
        return conversationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation", id));
    }

    public Conversation getOrCreateConversation(UUID partnerAId, UUID partnerBId) {
        return conversationRepository.findByPartnerAIdAndPartnerBId(partnerAId, partnerBId)
                .or(() -> conversationRepository.findByPartnerAIdAndPartnerBId(partnerBId, partnerAId))
                .orElseGet(() -> conversationRepository.save(
                        Conversation.builder().partnerAId(partnerAId).partnerBId(partnerBId).build()));
    }

    public void deleteConversation(UUID id) {
        Conversation conv = findConversationById(id);
        conv.setIsDeleted(true);
        conversationRepository.save(conv);
    }

    // --- Messages ---

    public List<Message> findMessagesByConversationId(UUID conversationId) {
        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public Message sendMessage(Message message) {
        return messageRepository.save(message);
    }

    public void deleteMessage(UUID id) {
        Message msg = messageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Message", id));
        msg.setIsDeleted(true);
        messageRepository.save(msg);
    }
}
