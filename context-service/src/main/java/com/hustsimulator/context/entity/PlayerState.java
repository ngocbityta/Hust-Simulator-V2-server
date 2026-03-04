package com.hustsimulator.context.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "player_states")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlayerState {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_state", nullable = false, columnDefinition = "player_activity_state")
    @Builder.Default
    private PlayerActivityState activityState = PlayerActivityState.ROAMING;

    @Column(name = "zone_id")
    private UUID zoneId;

    @Column(name = "session_data", columnDefinition = "jsonb")
    @Builder.Default
    private String sessionData = "{}";

    @Column(name = "entered_at")
    private LocalDateTime enteredAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
