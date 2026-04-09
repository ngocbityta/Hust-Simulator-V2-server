package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "scheduled_jobs",
       uniqueConstraints = @UniqueConstraint(columnNames = {"job_id", "job_type", "target_time"}))
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Job extends BaseEntity {

    @Column(name = "job_id", nullable = false)
    private String jobId;

    @Column(name = "job_type", nullable = false)
    private String jobType;

    @Column(name = "target_time", nullable = false)
    private LocalDateTime targetTime;

    @Column(nullable = false)
    @Builder.Default
    private String status = "PENDING";
}
