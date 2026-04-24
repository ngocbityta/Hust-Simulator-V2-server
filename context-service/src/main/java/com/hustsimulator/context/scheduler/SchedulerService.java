package com.hustsimulator.context.scheduler;

import java.time.LocalDateTime;
import com.hustsimulator.context.enums.JobType;

public interface SchedulerService {
    /**
     * Schedules a job for future execution.
     */
    boolean scheduleJob(String jobId, JobType jobType, LocalDateTime targetTime);

    /**
     * Marks a job as completed.
     */
    void markCompleted(String jobId, JobType jobType, LocalDateTime targetTime);

    /**
     * Recovers any missed/pending jobs that should have been executed.
     */
    void recoverMissedJobs();
}
