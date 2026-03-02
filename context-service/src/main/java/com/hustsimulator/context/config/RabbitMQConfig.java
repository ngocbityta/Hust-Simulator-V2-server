package com.hustsimulator.context.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnBean(ConnectionFactory.class)
public class RabbitMQConfig {

    // Exchange names
    public static final String SPATIAL_EVENTS_EXCHANGE = "hustsim.spatial.events";
    public static final String TEMPORAL_EVENTS_EXCHANGE = "hustsim.temporal.events";
    public static final String NOTIFICATION_EXCHANGE = "hustsim.notifications";

    // Queue names
    public static final String SPATIAL_TRIGGER_QUEUE = "hustsim.spatial.triggers";
    public static final String TEMPORAL_TRIGGER_QUEUE = "hustsim.temporal.triggers";
    public static final String NOTIFICATION_QUEUE = "hustsim.notifications";

    // --- Exchanges ---
    @Bean
    public TopicExchange spatialEventsExchange() {
        return new TopicExchange(SPATIAL_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange temporalEventsExchange() {
        return new TopicExchange(TEMPORAL_EVENTS_EXCHANGE);
    }

    @Bean
    public TopicExchange notificationExchange() {
        return new TopicExchange(NOTIFICATION_EXCHANGE);
    }

    // --- Queues ---
    @Bean
    public Queue spatialTriggerQueue() {
        return QueueBuilder.durable(SPATIAL_TRIGGER_QUEUE).build();
    }

    @Bean
    public Queue temporalTriggerQueue() {
        return QueueBuilder.durable(TEMPORAL_TRIGGER_QUEUE).build();
    }

    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE).build();
    }

    // --- Bindings ---
    @Bean
    public Binding spatialTriggerBinding(Queue spatialTriggerQueue, TopicExchange spatialEventsExchange) {
        return BindingBuilder.bind(spatialTriggerQueue).to(spatialEventsExchange).with("trigger.*");
    }

    @Bean
    public Binding temporalTriggerBinding(Queue temporalTriggerQueue, TopicExchange temporalEventsExchange) {
        return BindingBuilder.bind(temporalTriggerQueue).to(temporalEventsExchange).with("trigger.*");
    }

    @Bean
    public Binding notificationBinding(Queue notificationQueue, TopicExchange notificationExchange) {
        return BindingBuilder.bind(notificationQueue).to(notificationExchange).with("notify.*");
    }

    // --- Message Converter ---
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
