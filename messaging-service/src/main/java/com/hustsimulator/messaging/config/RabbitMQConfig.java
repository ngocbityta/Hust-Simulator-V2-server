package com.hustsimulator.messaging.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String REALTIME_EXCHANGE = "hust.realtime.exchange";
    public static final String REALTIME_QUEUE = "hust.realtime.queue";

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
}
