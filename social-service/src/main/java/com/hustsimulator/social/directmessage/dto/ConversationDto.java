package com.hustsimulator.social.directmessage.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationDto {
    private UUID id;
    private UUID partnerId;
    private String partnerName;
    private String partnerAvatar;
    private DirectMessageDto lastMessage;
    private long unreadCount;
}
