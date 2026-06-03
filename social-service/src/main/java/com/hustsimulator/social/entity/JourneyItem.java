package com.hustsimulator.social.entity;

import com.hustsimulator.social.common.BaseEntity;
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

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "journey_id", nullable = false)
    private Journey journey;

    @Column(name = "building_id")
    private UUID buildingId;

    @Column(name = "media_url", columnDefinition = "TEXT")
    private String mediaUrl;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "event_id")
    private UUID eventId;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "post_ids", columnDefinition = "uuid[]")
    private java.util.List<UUID> postIds;
}
