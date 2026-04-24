package com.hustsimulator.social.interaction;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
@Tag(name = "Interactions", description = "Post like / unlike operations")
public class InteractionController {

    private final InteractionService interactionService;

    @Operation(summary = "Like a post",
               description = "Idempotent: liking an already-liked post has no effect. Returns updated like count.")
    @PostMapping("/like")
    public Map<String, Object> like(@RequestBody InteractionDTO.LikeRequest request,
                                    @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        interactionService.likePost(request.postId(), userId);
        long likeCount = interactionService.countLikes(request.postId());
        return Map.of(
                "postId", request.postId(),
                "liked", true,
                "likeCount", likeCount
        );
    }

    @Operation(summary = "Unlike a post",
               description = "Idempotent: unliking a post that was not liked has no effect. Returns updated like count.")
    @PostMapping("/unlike")
    public Map<String, Object> unlike(@RequestBody InteractionDTO.LikeRequest request,
                                      @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        interactionService.unlikePost(request.postId(), userId);
        long likeCount = interactionService.countLikes(request.postId());
        return Map.of(
                "postId", request.postId(),
                "liked", false,
                "likeCount", likeCount
        );
    }

    @Operation(summary = "Get like count for a post")
    @GetMapping("/count/{postId}")
    public Map<String, Object> countLikes(@PathVariable UUID postId) {
        return Map.of(
                "postId", postId,
                "likeCount", interactionService.countLikes(postId)
        );
    }

    @Operation(summary = "Check if the current user has liked a post")
    @GetMapping("/has-liked/{postId}")
    public Map<String, Object> hasLiked(@PathVariable UUID postId,
                                        @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return Map.of(
                "postId", postId,
                "userId", userId,
                "hasLiked", interactionService.hasLiked(postId, userId)
        );
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
