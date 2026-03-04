package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "enrollments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Enrollment extends BaseEntity {

    @Column(name = "student_id")
    private UUID studentId;

    @Column(name = "teacher_id")
    private UUID teacherId;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    @Builder.Default
    private EnrollmentStatus status = EnrollmentStatus.PENDING;
}
