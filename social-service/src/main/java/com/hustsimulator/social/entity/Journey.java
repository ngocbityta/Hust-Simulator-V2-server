package com.hustsimulator.social.entity;

import com.hustsimulator.social.common.BaseEntity;
import com.hustsimulator.social.enums.JourneyStatus;
import com.hustsimulator.social.enums.JourneyVisibility;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "journeys", schema = "social")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Journey extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "journey_date", nullable = false)
    private LocalDate journeyDate;

    @Column(name = "video_url")
    private String videoUrl;

    @Column(name = "music_url")
    private String musicUrl;

    @Column(name = "template_id")
    private String templateId;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JourneyStatus status = JourneyStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private JourneyVisibility visibility = JourneyVisibility.PUBLIC;

    @OneToMany(mappedBy = "journey", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<JourneyItem> items = new ArrayList<>();
}
