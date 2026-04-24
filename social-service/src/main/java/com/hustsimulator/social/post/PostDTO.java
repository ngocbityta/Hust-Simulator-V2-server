package com.hustsimulator.social.post;

import java.util.UUID;
import com.hustsimulator.social.enums.PostStatus;

/**
 * Data Transfer Objects for Post operations.
 */
public class PostDTO {

    public record CreatePostRequest(
            String content,
            String videoUrl,
            Double latitude,
            Double longitude,
            UUID eventId,
            UUID buildingId,
            UUID roomId
    ) {}

    public record UpdatePostRequest(
            String content,
            String videoUrl,
            PostStatus status,
            String canEdit,
            String canComment
    ) {}
}
