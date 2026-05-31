package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "campus_ways")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusWay extends BaseEntity {

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "way_type", nullable = false, length = 50)
    private String wayType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String coordinates; // JSON: [[lng, lat], [lng, lat], ...]

    @Column(name = "distance_meters")
    private Double distanceMeters;

    @Column(name = "is_oneway")
    @Builder.Default
    private Boolean isOneway = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
