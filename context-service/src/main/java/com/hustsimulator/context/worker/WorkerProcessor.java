package com.hustsimulator.context.worker;

import java.util.Map;

/**
 * Strategy interface for processing matured jobs from the queue.
 */
public interface WorkerProcessor {
    /**
     * Checks if this processor can handle the given job type.
     */
    boolean supports(String workerType);

    /**
     * Executes the business logic for the job.
     */
    void process(Map<String, Object> payload);
}
