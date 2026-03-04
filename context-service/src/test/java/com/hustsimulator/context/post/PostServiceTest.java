package com.hustsimulator.context.post;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Post;
import com.hustsimulator.context.entity.PostVideo;
import org.junit.jupiter.api.BeforeEach;
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
class PostServiceTest {

    @Mock
    private PostRepository postRepository;

    @Mock
    private PostVideoRepository postVideoRepository;

    @InjectMocks
    private PostService postService;

    private Post testPost;
    private UUID postId;

    @BeforeEach
    void setUp() {
        postId = UUID.randomUUID();
        testPost = Post.builder()
                .authorId(UUID.randomUUID())
                .described("Test post")
                .build();
        testPost.setId(postId);
    }

    @Test
    void findAllPosts_shouldReturnList() {
        when(postRepository.findAll()).thenReturn(List.of(testPost));

        List<Post> result = postService.findAllPosts();

        assertThat(result).hasSize(1);
    }

    @Test
    void findPostById_shouldReturnPost() {
        when(postRepository.findById(postId)).thenReturn(Optional.of(testPost));

        Post result = postService.findPostById(postId);

        assertThat(result.getDescribed()).isEqualTo("Test post");
    }

    @Test
    void findPostById_shouldThrow_whenNotFound() {
        when(postRepository.findById(postId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> postService.findPostById(postId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createPost_shouldSave() {
        when(postRepository.save(any())).thenReturn(testPost);

        Post result = postService.createPost(testPost);

        assertThat(result).isNotNull();
        verify(postRepository).save(testPost);
    }

    @Test
    void deletePost_shouldThrow_whenNotExists() {
        when(postRepository.existsById(postId)).thenReturn(false);

        assertThatThrownBy(() -> postService.deletePost(postId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findVideosByPostId_shouldReturnVideos() {
        PostVideo video = PostVideo.builder().postId(postId).url("http://video.mp4").build();
        when(postVideoRepository.findByPostId(postId)).thenReturn(List.of(video));

        List<PostVideo> result = postService.findVideosByPostId(postId);

        assertThat(result).hasSize(1);
    }
}
