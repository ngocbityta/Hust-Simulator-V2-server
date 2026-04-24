package com.hustsimulator.context.worker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkerServiceTest {

    @Mock
    private WorkerProcessor processor;

    @Test
    void processJob_shouldDispatchToMatchingProcessor() {
        // Arrange
        when(processor.supports("START_CLASS")).thenReturn(true);
        WorkerServiceImpl service = new WorkerServiceImpl(List.of(processor));
        Map<String, Object> payload = Map.of("type", "START_CLASS", "jobId", "job-123");

        // Act
        service.processJob(payload);

        // Assert
        verify(processor).process(payload);
    }

    @Test
    void processJob_shouldNotDispatchToNonMatchingProcessor() {
        // Arrange
        when(processor.supports("END_CLASS")).thenReturn(false);
        WorkerServiceImpl service = new WorkerServiceImpl(List.of(processor));
        Map<String, Object> payload = Map.of("type", "END_CLASS", "jobId", "job-456");

        // Act
        service.processJob(payload);

        // Assert
        verify(processor, never()).process(any());
    }

    @Test
    void processJob_shouldHandleProcessorExceptions() {
        // Arrange
        when(processor.supports("ERROR_JOB")).thenReturn(true);
        doThrow(new RuntimeException("Fail")).when(processor).process(any());
        
        WorkerServiceImpl service = new WorkerServiceImpl(List.of(processor));
        Map<String, Object> payload = Map.of("type", "ERROR_JOB", "jobId", "job-fail");

        // Act
        service.processJob(payload);

        // Assert - should catch exception and log (we verify it reached the processor)
        verify(processor).process(payload);
    }
}
