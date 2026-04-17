package com.hustsimulator.social.usercache;

import com.hustsimulator.social.entity.UserCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Consumer lắng nghe User events từ RabbitMQ.
 * Khi Auth Service tạo/sửa/xóa User, listener này sẽ tự động
 * đồng bộ bản sao vào bảng user_cache của Social Service.
 *
 * → Social Service có thể tra cứu tên/avatar tác giả bài Post
 *   mà KHÔNG cần gọi HTTP sang Auth Service.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final UserCacheRepository userCacheRepository;

    @RabbitListener(queues = "social.user.queue")
    public void handleUserEvent(UserEvent event) {
        log.info("Received {} event for user {} (ID: {})",
                event.getEventType(), event.getUsername(), event.getUserId());

        switch (event.getEventType()) {
            case CREATED, UPDATED -> upsertUserCache(event);
            case DELETED -> deleteUserCache(event);
        }
    }

    private void upsertUserCache(UserEvent event) {
        UserCache cache = userCacheRepository.findById(event.getUserId())
                .orElse(new UserCache());

        cache.setId(event.getUserId());
        cache.setUsername(event.getUsername());
        cache.setPhonenumber(event.getPhonenumber());
        cache.setAvatar(event.getAvatar());
        cache.setCoverImage(event.getCoverImage());
        cache.setDescription(event.getDescription());
        cache.setRole(event.getRole());
        cache.setSyncedAt(LocalDateTime.now());

        userCacheRepository.save(cache);
        log.info("Synced user cache for user {} (ID: {})", event.getUsername(), event.getUserId());
    }

    private void deleteUserCache(UserEvent event) {
        userCacheRepository.deleteById(event.getUserId());
        log.info("Deleted user cache for user ID: {}", event.getUserId());
    }
}
