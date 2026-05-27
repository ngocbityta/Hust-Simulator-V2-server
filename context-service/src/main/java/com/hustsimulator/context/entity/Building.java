package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "buildings")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Building extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "map_id", nullable = false)
    private UUID mapId;

    @Column(columnDefinition = "TEXT", name = "coordinates", nullable = false)
    private String coordinates;

    @Column(name = "centroid_lat")
    private Double centroidLat;

    @Column(name = "centroid_lng")
    private Double centroidLng;

    @Column(name = "fill_color", length = 30)
    @Builder.Default
    private String fillColor = "179,167,154,230";

    @Column(name = "label_min_zoom")
    @Builder.Default
    private Double labelMinZoom = 15.0;

    @Column(name = "is_label_visible")
    @Builder.Default
    private Boolean isLabelVisible = true;

    @Column(name = "category", length = 50)
    @Builder.Default
    private String category = "OTHER";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
