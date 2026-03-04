package com.hustsimulator.context.enrollment;

import com.hustsimulator.context.entity.*;

import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;

    public List<Enrollment> findAll() {
        return enrollmentRepository.findAll();
    }

    public Enrollment findById(UUID id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
    }

    public List<Enrollment> findByStudentId(UUID studentId) {
        return enrollmentRepository.findByStudentId(studentId);
    }

    public List<Enrollment> findByTeacherId(UUID teacherId) {
        return enrollmentRepository.findByTeacherId(teacherId);
    }

    public Enrollment create(Enrollment enrollment) {
        return enrollmentRepository.save(enrollment);
    }

    public Enrollment updateStatus(UUID id, EnrollmentStatus status) {
        Enrollment enrollment = findById(id);
        enrollment.setStatus(status);
        return enrollmentRepository.save(enrollment);
    }

    public void delete(UUID id) {
        if (!enrollmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Enrollment", id);
        }
        enrollmentRepository.deleteById(id);
    }
}
