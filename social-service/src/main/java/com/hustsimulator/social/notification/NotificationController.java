package com.hustsimulator.social.notification;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Tag(name = "Notifications", description = "Notification management operations")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "Get notifications for the current user (paginated)")
    @GetMapping
    public Page<NotificationDTO.NotificationResponse> getNotifications(
            @RequestHeader("X-User-Id") String userIdHeader,
            Pageable pageable) {
        UUID userId = resolveUserId(userIdHeader);
        return notificationService.getNotifications(userId, pageable);
    }

    @Operation(summary = "Get unread notification count")
    @GetMapping("/unread-count")
    public Map<String, Object> getUnreadCount(@RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return Map.of("unreadCount", notificationService.getUnreadCount(userId));
    }

    @Operation(summary = "Mark a notification as read")
    @PutMapping("/{id}/read")
    public Map<String, Object> markAsRead(@PathVariable UUID id,
                                          @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        notificationService.markAsRead(id, userId);
        return Map.of("id", id, "isRead", true);
    }

    @Operation(summary = "Mark all notifications as read")
    @PutMapping("/read-all")
    public Map<String, Object> markAllAsRead(@RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        notificationService.markAllAsRead(userId);
        return Map.of("message", "All notifications marked as read");
    }

    private UUID resolveUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid X-User-Id format");
        }
    }
}
