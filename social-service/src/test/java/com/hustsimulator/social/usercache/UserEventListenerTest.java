package com.hustsimulator.social.usercache;

import com.hustsimulator.social.entity.UserCache;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
                .username("newuser")
                .avatar("avatar_url")
                .build();

        when(userCacheRepository.findById(userId)).thenReturn(Optional.empty());

        // When
        userEventListener.handleUserEvent(event);

        // Then
        ArgumentCaptor<UserCache> cacheCaptor = ArgumentCaptor.forClass(UserCache.class);
        verify(userCacheRepository).save(cacheCaptor.capture());
        
        UserCache savedCache = cacheCaptor.getValue();
        assertEquals(userId, savedCache.getId());
        assertEquals("newuser", savedCache.getUsername());
        assertEquals("avatar_url", savedCache.getAvatar());
    }

    @Test
    void handleUserEvent_UpdatedEvent_UpdatesExistingUserCache() {
        // Given
        UUID userId = UUID.randomUUID();
        UserEvent event = UserEvent.builder()
                .eventType(UserEvent.EventType.UPDATED)
                .userId(userId)
                .username("updated_name")
                .avatar("new_avatar")
                .build();

        UserCache existingCache = UserCache.builder()
                .id(userId)
                .username("old_name")
                .build();

        when(userCacheRepository.findById(userId)).thenReturn(Optional.of(existingCache));

        // When
        userEventListener.handleUserEvent(event);

        // Then
        verify(userCacheRepository).save(existingCache);
        assertEquals("updated_name", existingCache.getUsername());
        assertEquals("new_avatar", existingCache.getAvatar());
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
