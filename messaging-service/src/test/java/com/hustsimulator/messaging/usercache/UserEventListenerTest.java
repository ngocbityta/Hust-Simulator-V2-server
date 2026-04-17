package com.hustsimulator.messaging.usercache;

import com.hustsimulator.messaging.entity.UserCache;
import com.hustsimulator.messaging.usercache.UserCacheRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserEventListenerTest {

    @Mock
    private UserCacheRepository userCacheRepository;

    @InjectMocks
    private UserEventListener userEventListener;

    @Test
    void handleUserEvent_CreatedEvent_SavesNewUserCache() {
        // Given
        UUID userId = UUID.randomUUID();
        UserEvent event = UserEvent.builder()
                .eventType(UserEvent.EventType.CREATED)
                .userId(userId)
                .username("msg-user")
                .avatar("msg-avatar")
                .build();

        when(userCacheRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        userEventListener.handleUserEvent(event);

        // Then
        verify(userCacheRepository).save(any(UserCache.class));
    }

    @Test
    void handleUserEvent_DeletedEvent_DeletesUserCache() {
        // Given
        UUID userId = UUID.randomUUID();
        UserEvent event = UserEvent.builder()
                .eventType(UserEvent.EventType.DELETED)
                .userId(userId)
                .build();

        // When
        userEventListener.handleUserEvent(event);

        // Then
        verify(userCacheRepository).deleteById(userId);
    }
}
