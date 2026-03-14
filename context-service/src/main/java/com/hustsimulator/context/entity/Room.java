package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "rooms")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Room extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(name = "building_id", nullable = false)
    private UUID buildingId;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;
}
