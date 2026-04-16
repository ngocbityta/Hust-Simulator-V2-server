package com.hustsimulator.social.comment;

import java.util.UUID;

/**
 * Data Transfer Objects for Comment operations.
 */
public class CommentDTO {

    public record CreateCommentRequest(
            UUID postId,
            String content,
            Double score,
            String detailMistake
    ) {}

    public record UpdateCommentRequest(
            String content,
            Double score,
            String detailMistake
    ) {}
}
