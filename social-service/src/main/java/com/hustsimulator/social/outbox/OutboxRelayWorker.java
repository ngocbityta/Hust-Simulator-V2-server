package com.hustsimulator.social.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class OutboxRelayWorker {

    private final OutboxEventRepository outboxEventRepository;
    private final RabbitTemplate rabbitTemplate;

    // Exchange and routing key to publish outbox events to
    private static final String EXCHANGE = "social.events.exchange";
    private static final String ROUTING_KEY_PREFIX = "social.outbox.";

    @Scheduled(fixedDelay = 2000) // Run every 2 seconds
    @Transactional
    public void relayEvents() {
        List<OutboxEvent> events = outboxEventRepository.findByProcessedFalseOrderByCreatedAtAsc(PageRequest.of(0, 50));
        
        if (events.isEmpty()) {
            return;
        }

        log.info("Found {} unprocessed outbox events", events.size());

        for (OutboxEvent event : events) {
            try {
                String routingKey = ROUTING_KEY_PREFIX + event.getAggregateType().toLowerCase();
                
                // Publish to RabbitMQ
                rabbitTemplate.convertAndSend(EXCHANGE, routingKey, event.getPayload().toString());
                
                // Mark as processed
                event.setProcessed(true);
                outboxEventRepository.save(event);
                
                log.debug("Successfully relayed outbox event: {}", event.getId());
            } catch (Exception e) {
                // If RabbitMQ is down, this will throw an exception, and the transaction will rollback (if we throw)
                // Or we can just log and break, so it retries later.
                log.error("Failed to relay outbox event: {}. RabbitMQ might be down. Will retry later.", event.getId(), e);
                break; // Stop processing this batch
            }
        }
    }
}
