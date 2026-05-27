package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "campus_nodes")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampusNode extends BaseEntity {

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    @Column(name = "node_type", nullable = false, length = 50)
    private String nodeType;

    @Column(nullable = false)
    private Double latitude;

    @Column(nullable = false)
    private Double longitude;

    @Column(name = "building_id")
    private UUID buildingId;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
