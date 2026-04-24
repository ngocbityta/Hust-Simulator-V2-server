package com.hustsimulator.social.entity;

import com.hustsimulator.social.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;

import java.util.UUID;
import com.hustsimulator.social.enums.PostStatus;

@Entity
@Table(name = "posts")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Post extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "video_url")
    private String videoUrl;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PostStatus status = PostStatus.ACTIVE;

    @Column(columnDefinition = "geometry(Point,4326)")
    private Point location;

    @Builder.Default
    @Column(name = "can_edit")
    private String canEdit = "1";

    @Builder.Default
    @Column(name = "can_comment")
    private String canComment = "1";

    @Builder.Default
    private String banned = "0";

    /** Gắn với sự kiện (events table) */
    @Column(name = "event_id")
    private UUID eventId;

    /** Gắn với toà nhà (buildings table) */
    @Column(name = "building_id")
    private UUID buildingId;

    /** Gắn với phòng học (rooms table) */
    @Column(name = "room_id")
    private UUID roomId;
}
