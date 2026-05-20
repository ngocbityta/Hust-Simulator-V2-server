package com.hustsimulator.social.directmessage.service;

import com.hustsimulator.social.directmessage.dto.ConversationDto;
import com.hustsimulator.social.directmessage.dto.DirectMessageDto;
import com.hustsimulator.social.directmessage.repository.ConversationRepository;
import com.hustsimulator.social.directmessage.repository.DirectMessageRepository;
import com.hustsimulator.social.entity.Conversation;
import com.hustsimulator.social.entity.DirectMessage;
import com.hustsimulator.social.entity.UserCache;
import com.hustsimulator.social.usercache.UserCacheRepository;
import com.hustsimulator.social.realtime.RealTimeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DirectMessageServiceImpl implements DirectMessageService {

    private final ConversationRepository conversationRepository;
    private final DirectMessageRepository directMessageRepository;
    private final UserCacheRepository userCacheRepository;
    private final RealTimeService realTimeService;

    @Override
    @Transactional
    public DirectMessageDto sendMessage(UUID senderId, UUID receiverId, String content) {
        // Ensure receiver exists
        if (!userCacheRepository.existsById(receiverId)) {
            throw new IllegalArgumentException("Receiver does not exist");
        }

        Conversation conversation = conversationRepository.findByPartners(senderId, receiverId)
                .orElseGet(() -> {
                    Conversation newConv = Conversation.builder()
                            .partnerAId(senderId)
                            .partnerBId(receiverId)
                            .isDeleted(false)
                            .build();
                    return conversationRepository.save(newConv);
                });

        DirectMessage message = DirectMessage.builder()
                .conversation(conversation)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .isRead(false)
                .isDeleted(false)
                .build();

        DirectMessage savedMessage = directMessageRepository.save(message);
        DirectMessageDto dto = mapToDto(savedMessage);

        // Realtime update
        realTimeService.broadcast("user_" + receiverId, "direct:message", dto);
        
        return dto;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<DirectMessageDto> getConversationHistory(UUID userId, UUID partnerId, Pageable pageable) {
        Optional<Conversation> conversationOpt = conversationRepository.findByPartners(userId, partnerId);
        
        if (conversationOpt.isEmpty()) {
            return Page.empty();
        }

        return directMessageRepository.findByConversationIdOrderByCreatedAtDesc(conversationOpt.get().getId(), pageable)
                .map(this::mapToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConversationDto> getUserConversations(UUID userId) {
        List<Conversation> conversations = conversationRepository.findAllByUserId(userId);
        
        return conversations.stream().map(conv -> {
            UUID partnerId = conv.getPartnerAId().equals(userId) ? conv.getPartnerBId() : conv.getPartnerAId();
            
            String partnerName = "Unknown User";
            String partnerAvatar = null;
            Optional<UserCache> userOpt = userCacheRepository.findById(partnerId);
            if (userOpt.isPresent()) {
                partnerName = userOpt.get().getUsername();
                partnerAvatar = userOpt.get().getAvatar();
            }

            DirectMessageDto lastMessageDto = directMessageRepository.findFirstByConversationIdOrderByCreatedAtDesc(conv.getId())
                    .map(this::mapToDto)
                    .orElse(null);

            long unreadCount = directMessageRepository.countByConversationIdAndReceiverIdAndIsReadFalse(conv.getId(), userId);

            return ConversationDto.builder()
                    .id(conv.getId())
                    .partnerId(partnerId)
                    .partnerName(partnerName)
                    .partnerAvatar(partnerAvatar)
                    .lastMessage(lastMessageDto)
                    .unreadCount(unreadCount)
                    .build();
        }).collect(Collectors.toList());
    }

    private DirectMessageDto mapToDto(DirectMessage msg) {
        return DirectMessageDto.builder()
                .id(msg.getId())
                .conversationId(msg.getConversation().getId())
                .senderId(msg.getSenderId())
                .receiverId(msg.getReceiverId())
                .content(msg.getContent())
                .isRead(msg.getIsRead())
                .createdAt(msg.getCreatedAt())
                .build();
    }
}
