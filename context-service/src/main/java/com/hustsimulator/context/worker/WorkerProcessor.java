package com.hustsimulator.context.worker;

import java.util.Map;
import com.hustsimulator.context.enums.JobType;

/**
 * Strategy interface for processing matured jobs from the queue.
 */
public interface WorkerProcessor {
    /**
     * Checks if this processor can handle the given job type.
     */
    boolean supports(JobType workerType);

    /**
     * Executes the business logic for the job.
     */
    void process(Map<String, Object> payload);
}
