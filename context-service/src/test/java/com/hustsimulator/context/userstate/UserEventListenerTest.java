package com.hustsimulator.context.userstate;

import com.hustsimulator.context.entity.UserState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventListenerTest {

    @Mock
    private UserStateRepository userStateRepository;

    @InjectMocks
    private UserEventListener userEventListener;

    @Test
    void handleUserEvent_CreatedEvent_InitializesUserState() {
        // Given
        UUID userId = UUID.randomUUID();
        UserEvent event = UserEvent.builder()
                .eventType(UserEvent.EventType.CREATED)
                .userId(userId)
                .build();

        when(userStateRepository.findByUserId(userId)).thenReturn(Optional.empty());

        // When
        userEventListener.handleUserEvent(event);

        // Then
        verify(userStateRepository).save(any(UserState.class));
    }

    @Test
    void handleUserEvent_CreatedEvent_DoesNotDuplicateIfExistent() {
        // Given
        UUID userId = UUID.randomUUID();
        UserEvent event = UserEvent.builder()
                .eventType(UserEvent.EventType.CREATED)
                .userId(userId)
                .build();

        when(userStateRepository.findByUserId(userId)).thenReturn(Optional.of(new UserState()));

        // When
        userEventListener.handleUserEvent(event);

        // Then
        verify(userStateRepository, never()).save(any(UserState.class));
    }
}
