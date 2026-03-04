package com.hustsimulator.context.enrollment;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Enrollment;
import com.hustsimulator.context.entity.EnrollmentStatus;
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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private Enrollment testEnrollment;
    private UUID enrollmentId;

    @BeforeEach
    void setUp() {
        enrollmentId = UUID.randomUUID();
        testEnrollment = Enrollment.builder()
                .studentId(UUID.randomUUID())
                .teacherId(UUID.randomUUID())
                .status(EnrollmentStatus.PENDING)
                .build();
        testEnrollment.setId(enrollmentId);
    }

    @Test
    void findById_shouldReturnEnrollment() {
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(testEnrollment));

        Enrollment result = enrollmentService.findById(enrollmentId);

        assertThat(result.getStatus()).isEqualTo(EnrollmentStatus.PENDING);
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> enrollmentService.findById(enrollmentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateStatus_shouldChangeStatus() {
        when(enrollmentRepository.findById(enrollmentId)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any())).thenReturn(testEnrollment);

        enrollmentService.updateStatus(enrollmentId, EnrollmentStatus.ACCEPTED);

        assertThat(testEnrollment.getStatus()).isEqualTo(EnrollmentStatus.ACCEPTED);
        verify(enrollmentRepository).save(testEnrollment);
    }

    @Test
    void create_shouldSaveEnrollment() {
        when(enrollmentRepository.save(any())).thenReturn(testEnrollment);

        Enrollment result = enrollmentService.create(testEnrollment);

        assertThat(result).isNotNull();
        verify(enrollmentRepository).save(testEnrollment);
    }

    @Test
    void delete_shouldThrow_whenNotExists() {
        when(enrollmentRepository.existsById(enrollmentId)).thenReturn(false);

        assertThatThrownBy(() -> enrollmentService.delete(enrollmentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
