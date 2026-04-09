package com.hustsimulator.context.scheduler;

import com.hustsimulator.context.config.RabbitMQConfig;
import com.hustsimulator.context.entity.ScheduledJob;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.time.LocalDateTime;
import java.util.Collections;
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
    void scheduleJob_shouldSaveAndPublish() {
        String jobId = "event-1";
        String type = "START_CLASS";
        LocalDateTime targetTime = LocalDateTime.now().plusHours(1);

        when(schedulerRepository.findByJobIdAndJobTypeAndTargetTime(any(), any(), any()))
                .thenReturn(Optional.empty());
        when(schedulerRepository.save(any(ScheduledJob.class)))
                .thenAnswer(i -> i.getArgument(0));

        boolean result = schedulerService.scheduleJob(jobId, type, targetTime, Collections.emptyMap());

        assertThat(result).isTrue();
        verify(schedulerRepository).save(any(ScheduledJob.class));
        verify(rabbitTemplate).convertAndSend(eq(RabbitMQConfig.DELAY_EXCHANGE), eq(""), any(Map.class), any(org.springframework.amqp.core.MessagePostProcessor.class));
    }

    @Test
    void scheduleJob_shouldBeIdempotent() {
        String jobId = "event-1";
        String type = "START_CLASS";
        LocalDateTime targetTime = LocalDateTime.now().plusHours(1);

        when(schedulerRepository.findByJobIdAndJobTypeAndTargetTime(jobId, type, targetTime))
                .thenReturn(Optional.of(new ScheduledJob()));

        boolean result = schedulerService.scheduleJob(jobId, type, targetTime, null);

        assertThat(result).isFalse();
        verify(schedulerRepository, never()).save(any());
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(), any(org.springframework.amqp.core.MessagePostProcessor.class));
    }

    @Test
    void markCompleted_shouldUpdateStatus() {
        String jobId = "event-1";
        String type = "START_CLASS";
        LocalDateTime targetTime = LocalDateTime.now();
        ScheduledJob job = ScheduledJob.builder().status("PENDING").build();

        when(schedulerRepository.findByJobIdAndJobTypeAndTargetTime(jobId, type, targetTime))
                .thenReturn(Optional.of(job));

        schedulerService.markCompleted(jobId, type, targetTime);

        assertThat(job.getStatus()).isEqualTo("COMPLETED");
        verify(schedulerRepository).save(job);
    }

    @Test
    void recoverMissedJobs_shouldRePublishPendingJobs() {
        ScheduledJob job = ScheduledJob.builder()
                .jobId("missed-1")
                .jobType("START_CLASS")
                .status("PENDING")
                .targetTime(LocalDateTime.now().minusMinutes(5))
                .build();

        when(schedulerRepository.findByStatusAndTargetTimeBefore(eq("PENDING"), any()))
                .thenReturn(List.of(job));

        schedulerService.recoverMissedJobs();

        // Should publish once for the missed job, and check for future jobs (which won't find any in this mock)
        verify(rabbitTemplate, atLeastOnce()).convertAndSend(eq(RabbitMQConfig.DELAY_EXCHANGE), eq(""), any(Map.class), any(org.springframework.amqp.core.MessagePostProcessor.class));
    }
}
