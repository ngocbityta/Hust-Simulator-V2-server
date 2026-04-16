package com.hustsimulator.social.comment;

import com.hustsimulator.social.entity.Comment;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @GetMapping("/post/{postId}")
    public Page<Comment> findByPostId(@PathVariable UUID postId, Pageable pageable) {
        return commentService.findByPostId(postId, pageable);
    }

    @GetMapping("/user/{userId}")
    public Page<Comment> findByUserId(@PathVariable UUID userId, Pageable pageable) {
        return commentService.findByUserId(userId, pageable);
    }

    @GetMapping("/{id}")
    public Comment findById(@PathVariable UUID id) {
        return commentService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Comment create(@Valid @RequestBody CommentDTO.CreateCommentRequest request,
                          @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID userId = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.randomUUID();
        return commentService.create(request, userId);
    }

    @PutMapping("/{id}")
    public Comment update(@PathVariable UUID id,
                          @Valid @RequestBody CommentDTO.UpdateCommentRequest request,
                          @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID userId = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.randomUUID();
        return commentService.update(id, request, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
                       @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID userId = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.randomUUID();
        commentService.delete(id, userId);
    }
}
