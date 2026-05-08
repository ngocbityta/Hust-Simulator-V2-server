package com.hustsimulator.social.entity;

import com.hustsimulator.social.common.BaseEntity;
import com.hustsimulator.social.enums.JourneyItemType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "journey_items", schema = "social")
@Data
@EqualsAndHashCode(callSuper = true, exclude = "journey")
@ToString(exclude = "journey")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JourneyItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JourneyItemType type;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(name = "media_url", columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;
}
