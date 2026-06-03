package com.hustsimulator.context.entity;

import com.hustsimulator.context.enums.IssueCategory;
import com.hustsimulator.context.enums.IssueStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "facility_issues")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacilityIssue {

    @Id
    @Builder.Default
    private UUID id = UUID.randomUUID();

    @Column(name = "building_id", nullable = false)
    private UUID buildingId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "reporter_id", nullable = false)
    private UUID reporterId;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 50)
    private IssueCategory category;

    @Column(name = "description", nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 50)
    @Builder.Default
    private IssueStatus status = IssueStatus.OPEN;

    @Column(name = "resolved_by")
    private UUID resolvedBy;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;
}
