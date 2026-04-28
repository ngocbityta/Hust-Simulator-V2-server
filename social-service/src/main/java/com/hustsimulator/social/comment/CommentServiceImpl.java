package com.hustsimulator.social.comment;

import com.hustsimulator.social.common.ResourceNotFoundException;
import com.hustsimulator.social.entity.Comment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;

    @Override
    public Page<Comment> findByPostId(UUID postId, Pageable pageable) {
        return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
    }

    @Override
    public Page<Comment> findByUserId(UUID userId, Pageable pageable) {
        return commentRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Comment findById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }

    @Override
    @Transactional
    public Comment create(CommentDTO.CreateCommentRequest request, UUID userId) {
        log.info("Creating comment on post {} by user {}", request.postId(), userId);
        Comment comment = Comment.builder()
                .userId(userId)
                .postId(request.postId())
                .content(request.content())
                .build();
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public Comment update(UUID id, CommentDTO.UpdateCommentRequest request, UUID userId) {
        Comment comment = findById(id);
        
        if (!comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this comment");
        }

        if (request.content() != null) comment.setContent(request.content());

        log.info("Updating comment: {}", id);
        return commentRepository.save(comment);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID userId) {
        Comment comment = findById(id);
        
        if (!comment.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this comment");
        }

        commentRepository.delete(comment);
        log.info("Deleted comment: {}", id);
    }
}
