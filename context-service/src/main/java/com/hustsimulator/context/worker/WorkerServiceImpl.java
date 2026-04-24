package com.hustsimulator.context.worker;

import com.hustsimulator.context.enums.JobType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkerServiceImpl implements WorkerService {

    private final List<WorkerProcessor> processors;

    @Override
    public void processJob(Map<String, Object> payload) {
        String typeStr = (String) payload.get("type");
        JobType type;
        try {
            type = JobType.valueOf(typeStr);
        } catch (IllegalArgumentException e) {
            log.error("Unknown job type: {}", typeStr);
            return;
        }

        String jobId = (String) payload.get("jobId");

        log.info("WorkerService: Dispatching task '{}' of type '{}'", jobId, type);

        boolean handled = false;
        for (WorkerProcessor processor : processors) {
            if (processor.supports(type)) {
                try {
                    processor.process(payload);
                    handled = true;
                    log.info("Task '{}' processed successfully by {}", jobId, processor.getClass().getSimpleName());
                } catch (Exception e) {
                    log.error("Error in job processor {}: {}", processor.getClass().getSimpleName(), e.getMessage(), e);
                }
            }
        }

        if (!handled) {
            log.warn("No suitable JobProcessor found for type: {}", type);
        }
    }
}
