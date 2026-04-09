package com.hustsimulator.context.message;

import com.hustsimulator.context.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;

    @Override
    public Message save(UUID eventId, UUID senderId, String type, String content, UUID fileId) {
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
}
