package com.hustsimulator.social.entity;

import com.hustsimulator.social.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "comments")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(name = "post_id", nullable = false)
    private UUID postId;

    @Column(columnDefinition = "TEXT")
    private String content;

    private Double score;

    @Column(name = "detail_mistake", columnDefinition = "TEXT")
    private String detailMistake;
}
