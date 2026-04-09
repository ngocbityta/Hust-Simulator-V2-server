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

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
