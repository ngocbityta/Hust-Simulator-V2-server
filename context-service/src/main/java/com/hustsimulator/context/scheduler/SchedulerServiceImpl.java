package com.hustsimulator.context.scheduler;

import com.hustsimulator.context.config.RabbitMQConfig;
import com.hustsimulator.context.entity.ScheduledJob;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

    private final RabbitTemplate rabbitTemplate;
    private final SchedulerRepository schedulerRepository;

    @Override
    @Transactional
    public boolean scheduleJob(String jobId, String jobType, LocalDateTime targetTime) {
        if (schedulerRepository.findByJobIdAndJobTypeAndTargetTime(jobId, jobType, targetTime).isPresent()) {
            log.debug("SchedulerService: Job already scheduled: {} / {} @ {}", jobId, jobType, targetTime);
            return false;
        }

        ScheduledJob record = ScheduledJob.builder()
                .jobId(jobId)
                .jobType(jobType)
                .targetTime(targetTime)
                .status("PENDING")
                .build();
        schedulerRepository.save(record);

        publishToDelayQueue(jobId, jobType, targetTime);

        log.info("SchedulerService: Scheduled job '{}' of type '{}' for {}", jobId, jobType, targetTime);
        return true;
    }

    @Override
    @Transactional
    public void markCompleted(String jobId, String jobType, LocalDateTime targetTime) {
        schedulerRepository.findByJobIdAndJobTypeAndTargetTime(jobId, jobType, targetTime)
                .ifPresent(job -> {
                    job.setStatus("COMPLETED");
                    schedulerRepository.save(job);
                    log.info("SchedulerService: Marked job completed: {} / {}", jobId, jobType);
                });
    }

    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void recoverMissedJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledJob> missedJobs = schedulerRepository.findByStatusAndTargetTimeBefore("PENDING", now);

        if (!missedJobs.isEmpty()) {
            log.warn("SchedulerService: Recovering {} missed jobs from downtime", missedJobs.size());
            for (ScheduledJob job : missedJobs) {
                publishToDelayQueue(job.getJobId(), job.getJobType(), now);
                log.info("SchedulerService: Re-dispatched missed job: {} / {}", job.getJobId(), job.getJobType());
            }
        }

        // Also check for jobs maturing soon to ensure they are in RabbitMQ after a restart
        List<ScheduledJob> futureJobs = schedulerRepository.findByStatusAndTargetTimeBefore("PENDING", now.plusHours(1));
        for (ScheduledJob job : futureJobs) {
            if (job.getTargetTime().isAfter(now)) {
                publishToDelayQueue(job.getJobId(), job.getJobType(), job.getTargetTime());
                log.info("SchedulerService: Re-enqueued future job: {} / {} @ {}", job.getJobId(), job.getJobType(), job.getTargetTime());
            }
        }
    }

    private void publishToDelayQueue(String jobId, String jobType, LocalDateTime targetTime) {
        LocalDateTime now = LocalDateTime.now();
        long delayMs = Duration.between(now, targetTime).toMillis();
        if (delayMs < 0) delayMs = 0;
        final String delayStr = String.valueOf(delayMs);

        Map<String, Object> payload = new HashMap<>();
        payload.put("jobId", jobId);
        payload.put("type", jobType);
        payload.put("targetTime", targetTime.toString());


        rabbitTemplate.convertAndSend(
            RabbitMQConfig.DELAY_EXCHANGE,
            "",
            payload,
            message -> {
                message.getMessageProperties().setExpiration(delayStr);
                return message;
            }
        );
    }
}
