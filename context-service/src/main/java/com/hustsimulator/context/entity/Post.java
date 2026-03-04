package com.hustsimulator.context.entity;

import com.hustsimulator.context.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "posts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Column(name = "author_id")
    private UUID authorId;

    private String described;

    @Column(name = "course_id")
    private UUID courseId;

    @Column(name = "exercise_id")
    private UUID exerciseId;

    @Column(name = "time_series_poses", columnDefinition = "jsonb")
    private String timeSeriesPoses;

    @Column(name = "can_comment")
    @Builder.Default
    private Boolean canComment = true;

    @Column(name = "can_edit")
    @Builder.Default
    private Boolean canEdit = true;

    @Column(name = "is_banned")
    @Builder.Default
    private Boolean isBanned = false;
}
