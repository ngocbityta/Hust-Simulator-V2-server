package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "notifications")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification extends BaseEntity {

    @Column(name = "user_id")
    private UUID userId;

    private String type;

    @Column(name = "object_id")
    private UUID objectId;

    private String title;
    private String avatar;

    @Column(name = "group_type")
    private Integer groupType;

    @Column(name = "is_read")
    @Builder.Default
    private Boolean isRead = false;
}
