package com.hustsimulator.context.social;

import com.hustsimulator.context.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SocialController {

    private final SocialService socialService;

    // --- Likes ---

    @PostMapping("/likes")
    @ResponseStatus(HttpStatus.CREATED)
    public PostLike likePost(@RequestParam UUID userId, @RequestParam UUID postId) {
        return socialService.likePost(userId, postId);
    }

    @DeleteMapping("/likes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlikePost(@RequestParam UUID userId, @RequestParam UUID postId) {
        socialService.unlikePost(userId, postId);
    }

    @GetMapping("/likes/post/{postId}")
    public List<PostLike> findLikesByPost(@PathVariable UUID postId) {
        return socialService.findLikesByPostId(postId);
    }

    @GetMapping("/likes/post/{postId}/count")
    public long countLikes(@PathVariable UUID postId) {
        return socialService.countLikes(postId);
    }

    // --- Comments ---

    @GetMapping("/comments/post/{postId}")
    public List<Comment> findCommentsByPost(@PathVariable UUID postId) {
        return socialService.findCommentsByPostId(postId);
    }

    @GetMapping("/comments/{id}")
    public Comment findCommentById(@PathVariable UUID id) {
        return socialService.findCommentById(id);
    }

    @PostMapping("/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment createComment(@Valid @RequestBody Comment comment) {
        return socialService.createComment(comment);
    }

    @PutMapping("/comments/{id}")
    public Comment updateComment(@PathVariable UUID id, @Valid @RequestBody Comment comment) {
        return socialService.updateComment(id, comment);
    }

    @DeleteMapping("/comments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteComment(@PathVariable UUID id) {
        socialService.deleteComment(id);
    }
}
