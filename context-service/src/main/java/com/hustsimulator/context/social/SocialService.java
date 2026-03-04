package com.hustsimulator.context.social;

import com.hustsimulator.context.entity.*;

import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SocialService {

    private final PostLikeRepository postLikeRepository;
    private final CommentRepository commentRepository;

    // --- Likes ---

    public PostLike likePost(UUID userId, UUID postId) {
        return postLikeRepository.findByUserIdAndPostId(userId, postId)
                .orElseGet(() -> postLikeRepository.save(
                        PostLike.builder().userId(userId).postId(postId).build()));
    }

    public void unlikePost(UUID userId, UUID postId) {
        postLikeRepository.findByUserIdAndPostId(userId, postId)
                .ifPresent(postLikeRepository::delete);
    }

    public List<PostLike> findLikesByPostId(UUID postId) {
        return postLikeRepository.findByPostId(postId);
    }

    public long countLikes(UUID postId) {
        return postLikeRepository.countByPostId(postId);
    }

    // --- Comments ---

    public List<Comment> findCommentsByPostId(UUID postId) {
        return commentRepository.findByPostId(postId);
    }

    public Comment findCommentById(UUID id) {
        return commentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Comment", id));
    }

    public Comment createComment(Comment comment) {
        return commentRepository.save(comment);
    }

    public Comment updateComment(UUID id, Comment updated) {
        Comment comment = findCommentById(id);
        comment.setContent(updated.getContent());
        comment.setScore(updated.getScore());
        comment.setDetailMistake(updated.getDetailMistake());
        return commentRepository.save(comment);
    }

    public void deleteComment(UUID id) {
        if (!commentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Comment", id);
        }
        commentRepository.deleteById(id);
    }
}
