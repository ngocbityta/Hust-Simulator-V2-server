package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.entity.RecurringEvent;
import com.hustsimulator.context.scheduler.SchedulerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecurringEventScheduler {

    private final RecurringEventService recurringEventService;
    private final SchedulerService schedulerService;

    public static final String JOB_TYPE_START = "START_CLASS";
    public static final String JOB_TYPE_END = "END_CLASS";

    @Scheduled(fixedRate = 600000) // 10 minutes
    public void scanAndSchedule() {
        log.info("Modular Scheduler: Scanning for upcoming classes...");
        List<RecurringEvent> activeEvents = recurringEventService.findScheduled();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime windowEnd = now.plusMinutes(10);

        for (RecurringEvent event : activeEvents) {
            try {
                CronExpression cron = CronExpression.parse(event.getCronExpression());
                LocalDateTime nextFireTime = cron.next(now);

                if (nextFireTime != null && nextFireTime.isBefore(windowEnd)) {
                    // Schedule START
                    schedulerService.scheduleJob(
                        event.getId().toString(),
                        JOB_TYPE_START,
                        nextFireTime,
                        null
                    );

                    // Schedule END
                    LocalDateTime endTime = nextFireTime.plusMinutes(event.getDurationMinutes());
                    schedulerService.scheduleJob(
                        event.getId().toString(),
                        JOB_TYPE_END,
                        endTime,
                        null
                    );
                }
            } catch (Exception e) {
                log.error("Failed to parse cron for event '{}': {}", event.getName(), e.getMessage());
            }
        }
    }
}
