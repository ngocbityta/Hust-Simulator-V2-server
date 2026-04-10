package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "virtual_maps")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Map extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT", name = "coordinates", nullable = false)
    private String coordinates;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;
}
