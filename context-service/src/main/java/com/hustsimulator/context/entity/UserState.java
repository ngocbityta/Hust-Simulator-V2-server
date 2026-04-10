package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import com.hustsimulator.context.enums.UserActivityState;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_states")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserState extends BaseEntity {

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_state", nullable = false)
    @Builder.Default
    private UserActivityState activityState = UserActivityState.OUTSIDE_MAP;

    @Column(name = "map_id")
    private UUID mapId;

    @Column(name = "building_id")
    private UUID buildingId;

    @Column(name = "room_id")
    private UUID roomId;

    @Column(name = "event_id")
    private UUID eventId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "session_data", columnDefinition = "jsonb")
    @Builder.Default
    private String sessionData = "{}";

    @Column(name = "entered_at")
    private LocalDateTime enteredAt;
}
