package com.hustsimulator.context.post;

import com.hustsimulator.context.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public List<Post> findAll() {
        return postService.findAllPosts();
    }

    @GetMapping("/{id}")
    public Post findById(@PathVariable UUID id) {
        return postService.findPostById(id);
    }

    @GetMapping("/author/{authorId}")
    public List<Post> findByAuthor(@PathVariable UUID authorId) {
        return postService.findPostsByAuthor(authorId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@Valid @RequestBody Post post) {
        return postService.createPost(post);
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable UUID id, @Valid @RequestBody Post post) {
        return postService.updatePost(id, post);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        postService.deletePost(id);
    }

    // --- Video sub-resource ---

    @GetMapping("/{postId}/videos")
    public List<PostVideo> findVideos(@PathVariable UUID postId) {
        return postService.findVideosByPostId(postId);
    }

    @PostMapping("/{postId}/videos")
    @ResponseStatus(HttpStatus.CREATED)
    public PostVideo addVideo(@PathVariable UUID postId, @Valid @RequestBody PostVideo video) {
        video.setPostId(postId);
        return postService.addVideo(video);
    }

    @DeleteMapping("/videos/{videoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteVideo(@PathVariable UUID videoId) {
        postService.deleteVideo(videoId);
    }
}
