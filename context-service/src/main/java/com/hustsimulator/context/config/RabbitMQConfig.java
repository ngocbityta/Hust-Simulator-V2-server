package com.hustsimulator.context.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class RabbitMQConfig {

    public static final String DLX_EXCHANGE = "hust.job.active.exchange";
    public static final String DELAY_EXCHANGE = "hust.job.delay.exchange";
    
    public static final String ACTIVE_JOB_QUEUE = "hust.job.active.queue";
    public static final String DELAY_JOB_QUEUE = "hust.job.delay.queue";

    @Bean
    public FanoutExchange activeJobExchange() {
        return new FanoutExchange(DLX_EXCHANGE);
    }

    @Bean
    public Queue activeJobQueue() {
        return new Queue(ACTIVE_JOB_QUEUE);
    }

    @Bean
    public Binding activeJobBinding(Queue activeJobQueue, FanoutExchange activeJobExchange) {
        return BindingBuilder.bind(activeJobQueue).to(activeJobExchange);
    }

    @Bean
    public FanoutExchange delayJobExchange() {
        return new FanoutExchange(DELAY_EXCHANGE);
    }

    @Bean
    public Queue delayJobQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", DLX_EXCHANGE);
        return new Queue(DELAY_JOB_QUEUE, true, false, false, args);
    }

    @Bean
    public Binding delayJobBinding(Queue delayJobQueue, FanoutExchange delayJobExchange) {
        return BindingBuilder.bind(delayJobQueue).to(delayJobExchange);
    }
}
