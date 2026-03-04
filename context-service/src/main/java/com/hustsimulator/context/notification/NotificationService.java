package com.hustsimulator.context.notification;

import com.hustsimulator.context.entity.*;

import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    public List<Notification> findByUserId(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public List<Notification> findUnreadByUserId(UUID userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    public Notification create(Notification notification) {
        return notificationRepository.save(notification);
    }

    public Notification markAsRead(UUID id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
        notification.setIsRead(true);
        return notificationRepository.save(notification);
    }

    public void delete(UUID id) {
        notificationRepository.deleteById(id);
    }
}
