package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import com.hustsimulator.context.enums.JobType;
import com.hustsimulator.context.enums.JobStatus;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_jobs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "job_type", "target_time"}))
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ScheduledJob extends BaseEntity {

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @Enumerated(EnumType.STRING)
    @Column(name = "job_type", nullable = false)
    private JobType jobType;

    @Column(name = "target_time", nullable = false)
    private LocalDateTime targetTime;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private JobStatus status = JobStatus.PENDING;
}
