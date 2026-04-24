package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class EventEventPublisher {

    public static final String EVENT_EXCHANGE = "hustsimulator.event.exchange";
    private final RabbitTemplate rabbitTemplate;

    public void publish(Event event, EventEvent.EventType eventType) {
        EventEvent eventDto = EventEvent.builder()
                .eventType(eventType)
                .eventId(event.getId())
                .name(event.getName())
                .description(event.getDescription())
                .mapId(event.getMapId())
                .status(event.getStatus() != null ? event.getStatus().name() : null)
                .build();

        String routingKey = "event." + eventType.name().toLowerCase();

        rabbitTemplate.convertAndSend(EVENT_EXCHANGE, routingKey, eventDto);
        log.info("Published {} event for Event: {} (ID: {})", 
                eventType, event.getName(), event.getId());
    }
}
