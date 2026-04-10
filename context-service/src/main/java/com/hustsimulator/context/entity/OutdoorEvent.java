package com.hustsimulator.context.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Entity
@DiscriminatorValue("OUTDOOR")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class OutdoorEvent extends Event {

    @Column(name = "min_x")
    private Double minX;

    @Column(name = "min_y")
    private Double minY;

    @Column(name = "max_x")
    private Double maxX;

    @Column(name = "max_y")
    private Double maxY;
}
