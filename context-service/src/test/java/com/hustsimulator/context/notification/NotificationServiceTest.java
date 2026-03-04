package com.hustsimulator.context.notification;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Notification;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private NotificationService notificationService;

    @Test
    void findByUserId_shouldReturnNotifications() {
        UUID userId = UUID.randomUUID();
        Notification n = Notification.builder().userId(userId).title("New like").build();
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(n));

        List<Notification> result = notificationService.findByUserId(userId);

        assertThat(result).hasSize(1);
    }

    @Test
    void markAsRead_shouldSetIsReadTrue() {
        UUID id = UUID.randomUUID();
        Notification n = Notification.builder().userId(UUID.randomUUID()).title("Test").build();
        n.setId(id);
        when(notificationRepository.findById(id)).thenReturn(Optional.of(n));
        when(notificationRepository.save(n)).thenReturn(n);

        Notification result = notificationService.markAsRead(id);

        assertThat(result.getIsRead()).isTrue();
    }

    @Test
    void markAsRead_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(notificationRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> notificationService.markAsRead(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
