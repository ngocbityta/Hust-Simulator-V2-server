package com.hustsimulator.context.scheduler;

import java.time.LocalDateTime;
import java.util.Map;

public interface SchedulerService {
    /**
     * Schedules a job for future execution.
     */
    boolean scheduleJob(String jobId, String jobType, LocalDateTime targetTime, Map<String, String> metadata);

    /**
     * Marks a job as completed.
     */
    void markCompleted(String jobId, String jobType, LocalDateTime targetTime);

    /**
     * Recovers any missed/pending jobs that should have been executed.
     */
    void recoverMissedJobs();
}
