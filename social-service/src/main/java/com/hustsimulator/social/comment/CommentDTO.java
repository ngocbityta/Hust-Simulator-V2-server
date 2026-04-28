package com.hustsimulator.social.comment;

import java.util.UUID;

/**
 * Data Transfer Objects for Comment operations.
 */
public class CommentDTO {

    public record CreateCommentRequest(
            UUID postId,
            String content
    ) {}

    public record UpdateCommentRequest(
            String content
    ) {}
}
