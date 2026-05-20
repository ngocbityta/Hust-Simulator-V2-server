package com.hustsimulator.social.message;

import com.hustsimulator.social.entity.Message;
import java.util.List;
import java.util.UUID;
import com.hustsimulator.social.enums.MessageType;

public interface MessageService {
    Message save(UUID eventId, UUID senderId, MessageType type, String content, UUID fileId);
    List<Message> getHistory(UUID eventId);
    List<UUID> getParticipatedEventIds(UUID userId);
}
