package com.hustsimulator.social.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ configuration cho Social Service.
 * Social Service đóng vai trò Consumer — lắng nghe các User events
 * từ Auth Service thông qua Exchange → Queue binding.
 *
 * Flow: Auth Service → hustsimulator.user.exchange → social.user.queue → UserEventListener
 */
@Configuration
public class RabbitMQConfig {

    public static final String USER_EXCHANGE = "hustsimulator.user.exchange";
    public static final String USER_QUEUE = "social.user.queue";
    public static final String USER_ROUTING_KEY = "user.#";

    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userQueue() {
        // durable = true → Queue tồn tại kể cả khi RabbitMQ restart
        return QueueBuilder.durable(USER_QUEUE).build();
    }

    @Bean
    public Binding userBinding(Queue userQueue, TopicExchange userExchange) {
        // Bind queue vào exchange với routing key "user.#"
        // → nhận tất cả message có routing key bắt đầu bằng "user." (user.created, user.updated, user.deleted)
        return BindingBuilder.bind(userQueue).to(userExchange).with(USER_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}
