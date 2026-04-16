package com.hustsimulator.social.comment;

import com.hustsimulator.social.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CommentService {
    Page<Comment> findByPostId(UUID postId, Pageable pageable);
    Page<Comment> findByUserId(UUID userId, Pageable pageable);
    Comment findById(UUID id);
    Comment create(CommentDTO.CreateCommentRequest request, UUID userId);
    Comment update(UUID id, CommentDTO.UpdateCommentRequest request, UUID userId);
    void delete(UUID id, UUID userId);
}
