package com.hustsimulator.auth.user;

import com.hustsimulator.auth.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserEventPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private UserEventPublisher userEventPublisher;

    private static final String EXCHANGE = "hustsimulator.user.exchange";

    @Test
    void publish_CreatedEvent_SendsCorrectMessage() {
        // Given
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .username("testuser")
                .phonenumber("0123456789")
                .build();
        user.setId(userId);

        // When
        userEventPublisher.publish(user, UserEvent.EventType.CREATED);

        // Then
        ArgumentCaptor<UserEvent> eventCaptor = ArgumentCaptor.forClass(UserEvent.class);
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq("user.created"), eventCaptor.capture());

        UserEvent capturedEvent = eventCaptor.getValue();
        assertEquals(UserEvent.EventType.CREATED, capturedEvent.getEventType());
        assertEquals(userId, capturedEvent.getUserId());
        assertEquals("testuser", capturedEvent.getUsername());
    }

    @Test
    void publish_UpdatedEvent_SendsCorrectMessage() {
        // Given
        User user = User.builder()
                .username("updateduser")
                .build();
        user.setId(UUID.randomUUID());

        // When
        userEventPublisher.publish(user, UserEvent.EventType.UPDATED);

        // Then
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq("user.updated"), org.mockito.ArgumentMatchers.any(UserEvent.class));
    }

    @Test
    void publish_DeletedEvent_SendsCorrectMessage() {
        // Given
        User user = User.builder().build();
        user.setId(UUID.randomUUID());

        // When
        userEventPublisher.publish(user, UserEvent.EventType.DELETED);

        // Then
        verify(rabbitTemplate).convertAndSend(eq(EXCHANGE), eq("user.deleted"), org.mockito.ArgumentMatchers.any(UserEvent.class));
    }
}
