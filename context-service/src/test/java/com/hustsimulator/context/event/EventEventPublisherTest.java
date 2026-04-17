package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.IndoorEvent;
import com.hustsimulator.context.enums.EventStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private EventEventPublisher eventEventPublisher;

    private static final String EXCHANGE = "hustsimulator.event.exchange";

    @Test
    void publish_CreatedEvent_SendsCorrectMessage() {
        // Given
        UUID eventId = UUID.randomUUID();
        IndoorEvent event = new IndoorEvent();
        event.setId(eventId);
        event.setName("Hust Open Day");
        event.setStatus(EventStatus.SCHEDULED);

        // When
        eventEventPublisher.publish(event, EventEvent.EventType.CREATED);

        // Then
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq("event.created"),
                org.mockito.ArgumentMatchers.any(EventEvent.class));
    }
}
