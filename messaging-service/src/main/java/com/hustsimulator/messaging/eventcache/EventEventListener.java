package com.hustsimulator.messaging.eventcache;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/**
 * Listener nhận sự kiện Event (Sự kiện học tập/hoạt động) từ Context Service.
 * Khi một Event mới được tạo, Messaging Service có thể tự động chuẩn bị
 * các session chat hoặc ghi log để sẵn sàng phục vụ.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class EventEventListener {

    @RabbitListener(queues = "messaging.event.queue")
    public void handleEventEvent(EventEvent event) {
        log.info("Received {} event for Event: {} (ID: {}) in messaging-service",
                event.getEventType(), event.getName(), event.getEventId());

        if (event.getEventType() == EventEvent.EventType.CREATED) {
            log.info("Preparing chat session for new Event: {}", event.getName());
            // Logic khởi tạo chat room hoặc data liên quan có thể thêm ở đây
        }
    }
}
