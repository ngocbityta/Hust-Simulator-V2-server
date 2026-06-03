package com.hustsimulator.social.post;

import java.util.UUID;
import com.hustsimulator.social.enums.PostStatus;

/**
 * Data Transfer Objects for Post operations.
 */
public class PostDTO {

    public record CreatePostRequest(
            String content,
            java.util.List<String> imageUrls,
            java.util.List<String> videoUrls,
            Double latitude,
            Double longitude,
            UUID eventId,
            UUID buildingId,
            UUID roomId
    ) {}

    public record UpdatePostRequest(
            String content,
            java.util.List<String> imageUrls,
            java.util.List<String> videoUrls,
            PostStatus status,
            String canEdit,
            String canComment
    ) {}
}
