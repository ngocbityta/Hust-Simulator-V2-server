package com.hustsimulator.context.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import jakarta.persistence.FetchType;
import java.util.List;
import java.util.UUID;

@Entity
@DiscriminatorValue("INDOOR")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class IndoorEvent extends Event {

    @Column(name = "building_id")
    private UUID buildingId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "event_rooms", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "room_id")
    private List<UUID> roomIds;
}
