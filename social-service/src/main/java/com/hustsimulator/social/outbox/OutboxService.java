package com.hustsimulator.social.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void createEvent(String aggregateType, String aggregateId, String eventType, Object payload) {
        try {
            OutboxEvent event = OutboxEvent.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(objectMapper.valueToTree(payload))
                    .createdAt(ZonedDateTime.now())
                    .processed(false)
                    .build();
            outboxEventRepository.save(event);
            log.info("Saved outbox event: {} for aggregate: {}/{}", eventType, aggregateType, aggregateId);
        } catch (Exception e) {
            log.error("Failed to serialize payload for outbox event", e);
            throw new RuntimeException("Failed to serialize outbox event payload", e);
        }
    }
}
