package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import com.hustsimulator.context.enums.EventStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventScheduler {

    private final EventRepository eventRepository;
    private final EventEventPublisher eventPublisher;

    @Scheduled(fixedRate = 60000) // Every 1 minute
    public void updateEventStatuses() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Update SCHEDULED -> ONGOING
        List<Event> scheduledEvents = eventRepository.findByStatus(EventStatus.SCHEDULED);
        for (Event event : scheduledEvents) {
            if (event.getStartTime() != null && !event.getStartTime().isAfter(now)) {
                if (event.getEndTime() != null && event.getEndTime().isBefore(now)) {
                    event.setStatus(EventStatus.COMPLETED);
                    eventRepository.save(event);
                    eventPublisher.publish(event, EventEvent.EventType.UPDATED);
                    log.info("Event {} marked as COMPLETED (was SCHEDULED but time passed)", event.getId());
                } else {
                    event.setStatus(EventStatus.ONGOING);
                    eventRepository.save(event);
                    eventPublisher.publish(event, EventEvent.EventType.UPDATED);
                    log.info("Event {} marked as ONGOING", event.getId());
                }
            }
        }

        // 2. Update ONGOING -> COMPLETED
        List<Event> ongoingEvents = eventRepository.findByStatus(EventStatus.ONGOING);
        for (Event event : ongoingEvents) {
            if (event.getEndTime() != null && !event.getEndTime().isAfter(now)) {
                event.setStatus(EventStatus.COMPLETED);
                eventRepository.save(event);
                eventPublisher.publish(event, EventEvent.EventType.UPDATED);
                log.info("Event {} marked as COMPLETED", event.getId());
            }
        }
    }
}
