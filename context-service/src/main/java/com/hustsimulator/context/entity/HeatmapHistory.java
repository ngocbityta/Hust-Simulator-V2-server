package com.hustsimulator.context.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "heatmap_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HeatmapHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(name = "cell_x", nullable = false)
    private Integer cellX;

    @Column(name = "cell_y", nullable = false)
    private Integer cellY;

    @Column(name = "average_count", nullable = false)
    private Integer averageCount;

    @CreationTimestamp
    @Column(name = "recorded_at", nullable = false, updatable = false)
    private LocalDateTime recordedAt;
}
