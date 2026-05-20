package com.hustsimulator.social.notification;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Data Transfer Objects for Notification operations.
 */
public class NotificationDTO {

    public record NotificationResponse(
            UUID id,
            UUID senderId,
            String senderUsername,
            String senderAvatar,
            String type,
            UUID objectId,
            String title,
            boolean isRead,
            LocalDateTime createdAt
    ) {}
}
