package com.hustsimulator.context.worker;

import com.hustsimulator.context.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Message listener for RabbitMQ jobs.
 * Infrastructure component that delegates to WorkerService.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class WorkerListener {

    private final WorkerService workerService;

    @RabbitListener(queues = RabbitMQConfig.ACTIVE_JOB_QUEUE)
    public void onWorkerTaskMatured(Map<String, Object> payload) {
        log.info("WorkerListener: Received matured task from RabbitMQ");
        try {
            workerService.processJob(payload);
        } catch (Exception e) {
            log.error("WorkerListener: Critical failure in task processing: {}", e.getMessage(), e);
        }
    }
}
