package com.hustsimulator.social.notification;

import com.hustsimulator.social.entity.Notification;
import com.hustsimulator.social.entity.UserCache;
import com.hustsimulator.social.enums.NotificationType;
import com.hustsimulator.social.usercache.UserCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserCacheRepository userCacheRepository;

    @Override
    @Transactional
    public void createNotification(UUID recipientId, UUID senderId, NotificationType type,
                                   UUID objectId, String title) {
        // Don't notify yourself
        if (recipientId.equals(senderId)) {
            return;
        }

        // Look up sender info for avatar
        String avatar = userCacheRepository.findById(senderId)
                .map(UserCache::getAvatar)
                .orElse(null);

        Notification notification = Notification.builder()
                .userId(recipientId)
                .senderId(senderId)
                .type(type.name())
                .objectId(objectId)
                .title(title)
                .avatar(avatar)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Created {} notification for user {} from user {}", type, recipientId, senderId);
    }

    @Override
    public Page<NotificationDTO.NotificationResponse> getNotifications(UUID userId, Pageable pageable) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUserIdAndIsRead(userId, false);
    }

    @Override
    @Transactional
    public void markAsRead(UUID notificationId, UUID userId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Notification not found"));

        if (!notification.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Not your notification");
        }

        notification.setIsRead(true);
        notificationRepository.save(notification);
        log.info("Marked notification {} as read for user {}", notificationId, userId);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        int count = notificationRepository.markAllAsRead(userId);
        log.info("Marked {} notifications as read for user {}", count, userId);
    }

    private NotificationDTO.NotificationResponse toResponse(Notification n) {
        String senderUsername = null;
        String senderAvatar = null;

        if (n.getSenderId() != null) {
            UserCache sender = userCacheRepository.findById(n.getSenderId()).orElse(null);
            if (sender != null) {
                senderUsername = sender.getUsername();
                senderAvatar = sender.getAvatar();
            }
        }

        return new NotificationDTO.NotificationResponse(
                n.getId(),
                n.getSenderId(),
                senderUsername,
                senderAvatar,
                n.getType(),
                n.getObjectId(),
                n.getTitle(),
                Boolean.TRUE.equals(n.getIsRead()),
                n.getCreatedAt()
        );
    }
}
