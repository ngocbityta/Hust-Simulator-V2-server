package com.hustsimulator.social.post;

import com.hustsimulator.social.entity.Post;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @GetMapping
    public Page<Post> findAll(Pageable pageable) {
        return postService.findAll(pageable);
    }

    @GetMapping("/{id}")
    public Post findById(@PathVariable UUID id) {
        return postService.findById(id);
    }

    @GetMapping("/user/{userId}")
    public Page<Post> findByUserId(@PathVariable UUID userId, Pageable pageable) {
        return postService.findByUserId(userId, pageable);
    }

    @GetMapping("/event/{eventId}")
    public Page<Post> findByEventId(@PathVariable UUID eventId, Pageable pageable) {
        return postService.findByEventId(eventId, pageable);
    }

    @GetMapping("/building/{buildingId}")
    public Page<Post> findByBuildingId(@PathVariable UUID buildingId, Pageable pageable) {
        return postService.findByBuildingId(buildingId, pageable);
    }

    @GetMapping("/room/{roomId}")
    public Page<Post> findByRoomId(@PathVariable UUID roomId, Pageable pageable) {
        return postService.findByRoomId(roomId, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Post create(@Valid @RequestBody PostDTO.CreatePostRequest request, 
                        @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return postService.create(request, userId);
    }

    @PutMapping("/{id}")
    public Post update(@PathVariable UUID id, 
                        @Valid @RequestBody PostDTO.UpdatePostRequest request,
                        @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return postService.update(id, request, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id,
                       @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        postService.delete(id, userId);
    }

    private UUID resolveUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid X-User-Id format");
        }
    }
}
