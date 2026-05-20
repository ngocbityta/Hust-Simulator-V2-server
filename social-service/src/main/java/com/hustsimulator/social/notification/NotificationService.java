package com.hustsimulator.social.notification;

import com.hustsimulator.social.enums.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {

    /**
     * Create a notification for a recipient.
     *
     * @param recipientId who receives the notification
     * @param senderId    who triggered the action
     * @param type        notification type
     * @param objectId    related object (post, friendship, etc.)
     * @param title       human-readable description
     */
    void createNotification(UUID recipientId, UUID senderId, NotificationType type,
                            UUID objectId, String title);

    Page<NotificationDTO.NotificationResponse> getNotifications(UUID userId, Pageable pageable);

    long getUnreadCount(UUID userId);

    void markAsRead(UUID notificationId, UUID userId);

    void markAllAsRead(UUID userId);
}
