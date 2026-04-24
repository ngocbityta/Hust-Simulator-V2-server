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

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.scheduling.annotation.Scheduled;
import com.hustsimulator.context.enums.JobType;
import com.hustsimulator.context.enums.JobStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerServiceImpl implements SchedulerService {

    private final RabbitTemplate rabbitTemplate;
    private final SchedulerRepository schedulerRepository;

    @Override
    @Transactional
    public boolean scheduleJob(String jobId, JobType jobType, LocalDateTime targetTime) {
        if (schedulerRepository.findByJobIdAndJobTypeAndTargetTime(jobId, jobType, targetTime).isPresent()) {
            log.debug("SchedulerService: Job already scheduled: {} / {} @ {}", jobId, jobType, targetTime);
            return false;
        }

        ScheduledJob record = ScheduledJob.builder()
                .jobId(jobId)
                .jobType(jobType)
                .targetTime(targetTime)
                .status(JobStatus.PENDING)
                .build();
        schedulerRepository.save(record);

        // Dispatched via DB polling instead of delay queue to prevent head-of-line
        // blocking

        log.info("SchedulerService: Scheduled job '{}' of type '{}' for {}", jobId, jobType, targetTime);
        return true;
    }

    @Override
    @Transactional
    public void markCompleted(String jobId, JobType jobType, LocalDateTime targetTime) {
        schedulerRepository.findByJobIdAndJobTypeAndTargetTime(jobId, jobType, targetTime)
                .ifPresent(job -> {
                    job.setStatus(JobStatus.COMPLETED);
                    schedulerRepository.save(job);
                    log.info("SchedulerService: Marked job completed: {} / {}", jobId, jobType);
                });
    }

    @Override
    @EventListener(ApplicationReadyEvent.class)
    public void recoverMissedJobs() {
        dispatchMaturedJobs();
    }

    @Scheduled(fixedRate = 5000)
    @Transactional
    public void dispatchMaturedJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<ScheduledJob> maturedJobs = schedulerRepository.findByStatusAndTargetTimeBefore(JobStatus.PENDING, now);

        if (!maturedJobs.isEmpty()) {
            for (ScheduledJob job : maturedJobs) {
                dispatchToActiveQueue(job.getJobId(), job.getJobType(), job.getTargetTime());
                job.setStatus(JobStatus.DISPATCHED);
                schedulerRepository.save(job);
                log.info("SchedulerService: Dispatched matured job: {} / {}", job.getJobId(), job.getJobType());
            }
        }
    }

    private void dispatchToActiveQueue(String jobId, JobType jobType, LocalDateTime targetTime) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("jobId", jobId);
        payload.put("type", jobType.name());
        payload.put("targetTime", targetTime.toString());

        rabbitTemplate.convertAndSend(RabbitMQConfig.DLX_EXCHANGE, "", payload);
    }
}
