package com.hustsimulator.context.userstate;

import com.hustsimulator.context.entity.UserState;
import com.hustsimulator.context.enums.UserActivityState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventListener {

    private final UserStateRepository userStateRepository;

    @RabbitListener(queues = "context.user.queue")
    public void handleUserEvent(UserEvent event) {
        log.info("Received {} event for user ID: {} in context-service",
                event.getEventType(), event.getUserId());

        if (event.getEventType() == UserEvent.EventType.CREATED) {
            initializeUserState(event);
        } else if (event.getEventType() == UserEvent.EventType.DELETED) {
            deleteUserState(event);
        }
    }

    private void initializeUserState(UserEvent event) {
        if (userStateRepository.findByUserId(event.getUserId()).isPresent()) {
            log.info("UserState already exists for user ID: {}, skipping initialization", event.getUserId());
            return;
        }

        UserState state = UserState.builder()
                .userId(event.getUserId())
                .activityState(UserActivityState.OUTSIDE_MAP)
                .sessionData("{}")
                .enteredAt(LocalDateTime.now())
                .build();

        userStateRepository.save(state);
        log.info("Initialized default UserState for new user ID: {}", event.getUserId());
    }

    private void deleteUserState(UserEvent event) {
        userStateRepository.findByUserId(event.getUserId())
                .ifPresent(state -> {
                    userStateRepository.delete(state);
                    log.info("Deleted UserState for user ID: {}", event.getUserId());
                });
    }
}
