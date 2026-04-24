package com.hustsimulator.messaging.message;

import com.hustsimulator.messaging.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import com.hustsimulator.messaging.enums.MessageType;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    @Override
    public Message save(UUID eventId, UUID senderId, MessageType type, String content, UUID fileId) {
        Message message = Message.builder()
                .eventId(eventId)
                .senderId(senderId)
                .type(type)
                .content(content)
                .fileId(fileId)
                .build();
        message = messageRepository.save(message);
        log.info("Saved {} message '{}' for event '{}'", type, message.getId(), eventId);
        return message;
    }

    @Override
    public List<Message> getHistory(UUID eventId) {
        return messageRepository.findByEventIdOrderByCreatedAtAsc(eventId);
    }

    @Override
    public List<UUID> getParticipatedEventIds(UUID userId) {
        log.debug("Fetching participated event IDs for user {}", userId);
        return messageRepository.findDistinctEventIdsBySenderId(userId);
    }
}
