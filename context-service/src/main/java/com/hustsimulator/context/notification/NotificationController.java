package com.hustsimulator.context.notification;

import com.hustsimulator.context.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/user/{userId}")
    public List<Notification> findByUserId(@PathVariable UUID userId) {
        return notificationService.findByUserId(userId);
    }

    @GetMapping("/user/{userId}/unread")
    public List<Notification> findUnread(@PathVariable UUID userId) {
        return notificationService.findUnreadByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Notification create(@Valid @RequestBody Notification notification) {
        return notificationService.create(notification);
    }

    @PatchMapping("/{id}/read")
    public Notification markAsRead(@PathVariable UUID id) {
        return notificationService.markAsRead(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        notificationService.delete(id);
    }
}
