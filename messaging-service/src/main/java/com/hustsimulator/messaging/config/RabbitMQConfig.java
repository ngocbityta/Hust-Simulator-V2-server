package com.hustsimulator.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REALTIME_EXCHANGE = "hust.realtime.exchange";
    public static final String REALTIME_QUEUE = "hust.realtime.queue";

    public static final String USER_EXCHANGE = "hustsimulator.user.exchange";
    public static final String USER_QUEUE = "messaging.user.queue";
    public static final String USER_ROUTING_KEY = "user.#";

    @Bean
    public FanoutExchange realtimeExchange() {
        return new FanoutExchange(REALTIME_EXCHANGE);
    }

    @Bean
    public Queue realtimeQueue() {
        return new Queue(REALTIME_QUEUE);
    }

    @Bean
    public Binding realtimeBinding(Queue realtimeQueue, FanoutExchange realtimeExchange) {
        return BindingBuilder.bind(realtimeQueue).to(realtimeExchange);
    }

    // User Sync Config
    @Bean
    public TopicExchange userExchange() {
        return new TopicExchange(USER_EXCHANGE);
    }

    @Bean
    public Queue userQueue() {
        return QueueBuilder.durable(USER_QUEUE).build();
    }

    @Bean
    public Binding userBinding(Queue userQueue, TopicExchange userExchange) {
        return BindingBuilder.bind(userQueue).to(userExchange).with(USER_ROUTING_KEY);
    }

    // Event Sync Config (Context -> Messaging)
    public static final String EVENT_EXCHANGE = "hustsimulator.event.exchange";
    public static final String EVENT_QUEUE = "messaging.event.queue";
    public static final String EVENT_ROUTING_KEY = "event.#";

    @Bean
    public TopicExchange eventExchange() {
        return new TopicExchange(EVENT_EXCHANGE);
    }

    @Bean
    public Queue eventQueue() {
        return QueueBuilder.durable(EVENT_QUEUE).build();
    }

    @Bean
    public Binding eventBinding(Queue eventQueue, TopicExchange eventExchange) {
        return BindingBuilder.bind(eventQueue).to(eventExchange).with(EVENT_ROUTING_KEY);
    }

    @Bean
    public org.springframework.amqp.support.converter.MessageConverter jsonMessageConverter() {
        return new org.springframework.amqp.support.converter.Jackson2JsonMessageConverter();
    }
}


