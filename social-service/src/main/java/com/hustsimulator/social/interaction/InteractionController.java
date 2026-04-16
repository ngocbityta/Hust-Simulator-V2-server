package com.hustsimulator.social.interaction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping("/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@RequestBody InteractionDTO.LikeRequest request,
                     @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        interactionService.likePost(request.postId(), userId);
    }

    @PostMapping("/unlike")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@RequestBody InteractionDTO.LikeRequest request,
                       @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        interactionService.unlikePost(request.postId(), userId);
    }

    @GetMapping("/count/{postId}")
    public Map<String, Object> countLikes(@PathVariable UUID postId) {
        return Map.of(
                "postId", postId,
                "likeCount", interactionService.countLikes(postId)
        );
    }

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
