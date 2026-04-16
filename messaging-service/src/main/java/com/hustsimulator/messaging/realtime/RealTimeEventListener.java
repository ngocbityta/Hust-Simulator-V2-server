package com.hustsimulator.messaging.realtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Listens to RabbitMQ events published by context-service
 * and broadcasts them via Socket.IO to connected clients.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RealTimeEventListener {

    private final RealTimeService realTimeService;

    @RabbitListener(queues = "#{realtimeQueue.name}")
    public void onRealtimeEvent(Map<String, Object> payload) {
        String room = (String) payload.get("room");
        String event = (String) payload.get("event");
        Object data = payload.get("data");

        log.info("Received realtime event from RabbitMQ: room={}, event={}", room, event);

        if (room != null && event != null) {
            realTimeService.broadcast(room, event, data);
        }
    }
}
