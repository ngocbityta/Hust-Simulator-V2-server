package com.hustsimulator.context.social;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Comment;
import com.hustsimulator.context.entity.PostLike;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SocialServiceTest {

    @Mock private PostLikeRepository postLikeRepository;
    @Mock private CommentRepository commentRepository;
    @InjectMocks private SocialService socialService;

    @Test
    void likePost_shouldCreateLike_whenNotExists() {
        UUID userId = UUID.randomUUID(), postId = UUID.randomUUID();
        PostLike like = PostLike.builder().userId(userId).postId(postId).build();
        when(postLikeRepository.findByUserIdAndPostId(userId, postId)).thenReturn(Optional.empty());
        when(postLikeRepository.save(any())).thenReturn(like);

        PostLike result = socialService.likePost(userId, postId);

        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    void likePost_shouldReturnExisting_whenAlreadyLiked() {
        UUID userId = UUID.randomUUID(), postId = UUID.randomUUID();
        PostLike existing = PostLike.builder().userId(userId).postId(postId).build();
        when(postLikeRepository.findByUserIdAndPostId(userId, postId)).thenReturn(Optional.of(existing));

        PostLike result = socialService.likePost(userId, postId);

        assertThat(result).isEqualTo(existing);
        verify(postLikeRepository, never()).save(any());
    }

    @Test
    void unlikePost_shouldDelete_whenExists() {
        UUID userId = UUID.randomUUID(), postId = UUID.randomUUID();
        PostLike like = PostLike.builder().userId(userId).postId(postId).build();
        when(postLikeRepository.findByUserIdAndPostId(userId, postId)).thenReturn(Optional.of(like));

        socialService.unlikePost(userId, postId);

        verify(postLikeRepository).delete(like);
    }

    @Test
    void countLikes_shouldReturnCount() {
        UUID postId = UUID.randomUUID();
        when(postLikeRepository.countByPostId(postId)).thenReturn(5L);

        long count = socialService.countLikes(postId);

        assertThat(count).isEqualTo(5);
    }

    @Test
    void findCommentsByPostId_shouldReturnComments() {
        UUID postId = UUID.randomUUID();
        Comment comment = Comment.builder().postId(postId).content("Great!").build();
        when(commentRepository.findByPostId(postId)).thenReturn(List.of(comment));

        List<Comment> result = socialService.findCommentsByPostId(postId);

        assertThat(result).hasSize(1);
    }

    @Test
    void findCommentById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(commentRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> socialService.findCommentById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteComment_shouldThrow_whenNotExists() {
        UUID id = UUID.randomUUID();
        when(commentRepository.existsById(id)).thenReturn(false);

        assertThatThrownBy(() -> socialService.deleteComment(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
