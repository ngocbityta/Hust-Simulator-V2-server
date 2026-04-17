package com.hustsimulator.auth.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration cho Auth Service.
 * Auth Service đóng vai trò Publisher — bắn các sự kiện User (Created/Updated/Deleted)
 * lên Exchange để các service khác lắng nghe.
 */
@Configuration
public class RabbitMQConfig {

    public static final String USER_EXCHANGE = "hustsimulator.user.exchange";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    /**
     * Chuyển đổi message sang JSON thay vì Java serialization mặc định,
     * giúp các service viết bằng ngôn ngữ khác cũng đọc được.
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
