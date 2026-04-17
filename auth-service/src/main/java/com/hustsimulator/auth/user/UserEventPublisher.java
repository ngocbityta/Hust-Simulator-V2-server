package com.hustsimulator.auth.user;

import com.hustsimulator.auth.config.RabbitMQConfig;
import com.hustsimulator.auth.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

/**
 * Publisher chịu trách nhiệm gửi User events lên RabbitMQ.
 * Các service khác (Social, Messaging, Context) sẽ lắng nghe các event này
 * để cập nhật bản sao dữ liệu User trong database riêng của chúng.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class UserEventPublisher {

    private final RabbitTemplate rabbitTemplate;

    /**
     * Chuyển đổi Entity User thành DTO UserEvent và publish lên Exchange.
     * Routing key format: user.created, user.updated, user.deleted
     */
    public void publish(User user, UserEvent.EventType eventType) {
        UserEvent event = UserEvent.builder()
                .eventType(eventType)
                .userId(user.getId())
                .username(user.getUsername())
                .phonenumber(user.getPhonenumber())
                .avatar(user.getAvatar())
                .coverImage(user.getCoverImage())
                .description(user.getDescription())
                .role(user.getRole() != null ? user.getRole().name() : null)
                .build();

        String routingKey = "user." + eventType.name().toLowerCase();

        rabbitTemplate.convertAndSend(
                RabbitMQConfig.USER_EXCHANGE,
                routingKey,
                event
        );

        log.info("Published {} event for user {} (ID: {})", eventType, user.getUsername(), user.getId());
    }
}
