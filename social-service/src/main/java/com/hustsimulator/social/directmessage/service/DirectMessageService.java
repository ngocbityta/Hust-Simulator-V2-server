package com.hustsimulator.social.directmessage.service;

import com.hustsimulator.social.directmessage.dto.ConversationDto;
import com.hustsimulator.social.directmessage.dto.DirectMessageDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface DirectMessageService {
    DirectMessageDto sendMessage(UUID senderId, UUID receiverId, String content);
    Page<DirectMessageDto> getConversationHistory(UUID userId, UUID partnerId, Pageable pageable);
    List<ConversationDto> getUserConversations(UUID userId);
}
