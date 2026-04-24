package com.hustsimulator.context.scheduler;

import com.hustsimulator.context.config.RabbitMQConfig;
import com.hustsimulator.context.entity.ScheduledJob;
import com.hustsimulator.context.enums.JobStatus;
import com.hustsimulator.context.enums.JobType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SchedulerServiceTest {

    @Mock private RabbitTemplate rabbitTemplate;
    @Mock private SchedulerRepository schedulerRepository;

    private SchedulerServiceImpl schedulerService;

    @BeforeEach
    void setUp() {
        schedulerService = new SchedulerServiceImpl(rabbitTemplate, schedulerRepository);
    }

    @Test
    void scheduleJob_shouldSaveRecord() {
        String jobId = "event-1";
        JobType type = JobType.START_CLASS;
        LocalDateTime targetTime = LocalDateTime.now().plusHours(1);

        when(schedulerRepository.findByJobIdAndJobTypeAndTargetTime(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(schedulerRepository.save(any(ScheduledJob.class)))
                .thenAnswer(i -> i.getArgument(0));

        boolean result = schedulerService.scheduleJob(jobId, type, targetTime);

        assertThat(result).isTrue();
        verify(schedulerRepository).save(any(ScheduledJob.class));
    }

    @Test
    void scheduleJob_shouldBeIdempotent() {
        String jobId = "event-1";
        JobType type = JobType.START_CLASS;
        LocalDateTime targetTime = LocalDateTime.now().plusHours(1);

        when(schedulerRepository.findByJobIdAndJobTypeAndTargetTime(jobId, type, targetTime))
                .thenReturn(Optional.of(new ScheduledJob()));

        boolean result = schedulerService.scheduleJob(jobId, type, targetTime);

        assertThat(result).isFalse();
        verify(schedulerRepository, never()).save(any());
    }

    @Test
    void markCompleted_shouldUpdateStatus() {
        String jobId = "event-1";
        JobType type = JobType.START_CLASS;
        LocalDateTime targetTime = LocalDateTime.now();
        ScheduledJob job = ScheduledJob.builder().status(JobStatus.PENDING).build();

        when(schedulerRepository.findByJobIdAndJobTypeAndTargetTime(jobId, type, targetTime))
                .thenReturn(Optional.of(job));

        schedulerService.markCompleted(jobId, type, targetTime);

        assertThat(job.getStatus()).isEqualTo(JobStatus.COMPLETED);
        verify(schedulerRepository).save(job);
    }

    @Test
    void dispatchMaturedJobs_shouldDispatchAndMarkDispatched() {
        ScheduledJob job = ScheduledJob.builder()
                .jobId("missed-1")
                .jobType(JobType.START_CLASS)
                .status(JobStatus.PENDING)
                .targetTime(LocalDateTime.now().minusMinutes(5))
                .build();

        when(schedulerRepository.findByStatusAndTargetTimeBefore(eq(JobStatus.PENDING), any()))
                .thenReturn(List.of(job));

        schedulerService.dispatchMaturedJobs();

        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.DLX_EXCHANGE), eq(""), any(Map.class));
        assertThat(job.getStatus()).isEqualTo(JobStatus.DISPATCHED);
        verify(schedulerRepository).save(job);
    }
}
