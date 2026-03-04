package com.hustsimulator.context.enrollment;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID> {

    List<Enrollment> findByStudentId(UUID studentId);

    List<Enrollment> findByTeacherId(UUID teacherId);

    List<Enrollment> findByStatus(EnrollmentStatus status);
}
