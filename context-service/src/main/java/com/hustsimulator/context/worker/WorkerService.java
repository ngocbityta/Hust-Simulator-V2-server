package com.hustsimulator.context.worker;

import java.util.Map;

/**
 * Service for managing and dispatching worker jobs.
 */
public interface WorkerService {
    /**
     * Entry point for processing a job payload.
     * Decouples the listener from specific job logic.
     */
    void processJob(Map<String, Object> payload);
}
