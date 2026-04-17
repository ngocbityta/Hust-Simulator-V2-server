package com.hustsimulator.messaging.usercache;

import com.hustsimulator.messaging.entity.UserCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final UserCacheRepository userCacheRepository;

    @RabbitListener(queues = "messaging.user.queue")
    public void handleUserEvent(UserEvent event) {
        log.info("Received {} event for user {} (ID: {}) in messaging-service",
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
        cache.setAvatar(event.getAvatar());
        cache.setSyncedAt(LocalDateTime.now());

        userCacheRepository.save(cache);
        log.info("Synced user cache in messaging-service for user {} (ID: {})", 
                event.getUsername(), event.getUserId());
    }

    private void deleteUserCache(UserEvent event) {
        userCacheRepository.deleteById(event.getUserId());
        log.info("Deleted user cache in messaging-service for user ID: {}", event.getUserId());
    }
}
