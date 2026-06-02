package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import com.hustsimulator.context.enums.RoomStatus;
import jakarta.persistence.*;
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

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.EMPTY;

    @Column
    private Integer capacity;

    @Column(name = "floor_num")
    private Integer floorNum;

    @Column(length = 50)
    private String type;

    @Column(name = "phone_num", length = 50)
    private String phoneNum;

    @Column(length = 100)
    private String email;

    @Column(length = 255)
    private String website;

}
