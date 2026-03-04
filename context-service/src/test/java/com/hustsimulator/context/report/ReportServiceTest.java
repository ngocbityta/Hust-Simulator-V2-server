package com.hustsimulator.context.report;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Report;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportService reportService;

    private Report testReport;
    private UUID reportId;

    @BeforeEach
    void setUp() {
        reportId = UUID.randomUUID();
        testReport = Report.builder()
                .userId(UUID.randomUUID())
                .postId(UUID.randomUUID())
                .subject("Spam")
                .details("This is spam content")
                .build();
        testReport.setId(reportId);
    }

    @Test
    void findById_shouldReturnReport() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.of(testReport));

        Report result = reportService.findById(reportId);

        assertThat(result.getSubject()).isEqualTo("Spam");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(reportRepository.findById(reportId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reportService.findById(reportId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findByPostId_shouldReturnReports() {
        UUID postId = testReport.getPostId();
        when(reportRepository.findByPostId(postId)).thenReturn(List.of(testReport));

        List<Report> result = reportService.findByPostId(postId);

        assertThat(result).hasSize(1);
    }

    @Test
    void create_shouldSaveReport() {
        when(reportRepository.save(testReport)).thenReturn(testReport);

        Report result = reportService.create(testReport);

        assertThat(result).isNotNull();
    }

    @Test
    void delete_shouldThrow_whenNotExists() {
        when(reportRepository.existsById(reportId)).thenReturn(false);

        assertThatThrownBy(() -> reportService.delete(reportId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
