package com.hustsimulator.context.post;

import com.hustsimulator.context.entity.*;

import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostVideoRepository postVideoRepository;

    // --- Post CRUD ---

    public List<Post> findAllPosts() {
        return postRepository.findAll();
    }

    public Post findPostById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
    }

    public List<Post> findPostsByAuthor(UUID authorId) {
        return postRepository.findByAuthorId(authorId);
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public Post updatePost(UUID id, Post updated) {
        Post post = findPostById(id);
        post.setDescribed(updated.getDescribed());
        post.setCanComment(updated.getCanComment());
        post.setCanEdit(updated.getCanEdit());
        post.setIsBanned(updated.getIsBanned());
        return postRepository.save(post);
    }

    public void deletePost(UUID id) {
        if (!postRepository.existsById(id)) {
            throw new ResourceNotFoundException("Post", id);
        }
        postRepository.deleteById(id);
    }

    // --- PostVideo CRUD ---

    public List<PostVideo> findVideosByPostId(UUID postId) {
        return postVideoRepository.findByPostId(postId);
    }

    public PostVideo addVideo(PostVideo video) {
        return postVideoRepository.save(video);
    }

    public void deleteVideo(UUID videoId) {
        postVideoRepository.deleteById(videoId);
    }
}
