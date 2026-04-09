package com.hustsimulator.context.worker;

import com.hustsimulator.context.realtime.RealTimeService;
import com.hustsimulator.context.recurringevent.RecurringEventScheduler;
import com.hustsimulator.context.recurringevent.RecurringEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClassRoomWorkerProcessor implements WorkerProcessor {

    private final RecurringEventService recurringEventService;
    private final RealTimeService realTimeService;

    @Override
    public boolean supports(String workerType) {
        return RecurringEventScheduler.JOB_TYPE_START.equals(workerType) || 
               RecurringEventScheduler.JOB_TYPE_END.equals(workerType);
    }

    @Override
    public void process(Map<String, Object> payload) {
        String workerType = (String) payload.get("type");
        if (RecurringEventScheduler.JOB_TYPE_START.equals(workerType)) {
            handleStartClass(payload);
        } else if (RecurringEventScheduler.JOB_TYPE_END.equals(workerType)) {
            handleEndClass(payload);
        }
    }

    private void handleStartClass(Map<String, Object> payload) {
        String eventIdStr = (String) payload.get("jobId");
        UUID eventId = UUID.fromString(eventIdStr);
        
        log.info("ClassRoomWorkerProcessor: Activating class {}", eventId);
        recurringEventService.activateClass(eventId);
        
        // Notify via Socket.io
        realTimeService.broadcast("class_" + eventIdStr, "class:started", eventIdStr);
    }

    private void handleEndClass(Map<String, Object> payload) {
        String eventIdStr = (String) payload.get("jobId");
        UUID eventId = UUID.fromString(eventIdStr);
        
        log.info("ClassRoomWorkerProcessor: Completing class {}", eventId);
        recurringEventService.completeClass(eventId);
        
        // Notify via Socket.io
        realTimeService.broadcast("class_" + eventIdStr, "class:ended", eventIdStr);
    }
}
