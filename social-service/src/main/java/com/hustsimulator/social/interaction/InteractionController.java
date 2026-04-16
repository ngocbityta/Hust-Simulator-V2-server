package com.hustsimulator.social.interaction;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/interactions")
@RequiredArgsConstructor
public class InteractionController {

    private final InteractionService interactionService;

    @PostMapping("/like")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void like(@RequestBody LikeDTO.LikeRequest request,
                     @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID userId = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.randomUUID();
        interactionService.likePost(request.postId(), userId);
    }

    @PostMapping("/unlike")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unlike(@RequestBody LikeDTO.LikeRequest request,
                       @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID userId = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.randomUUID();
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
                                        @RequestHeader(value = "X-User-Id", required = false) String userIdHeader) {
        UUID userId = userIdHeader != null ? UUID.fromString(userIdHeader) : UUID.randomUUID();
        return Map.of(
                "postId", postId,
                "userId", userId,
                "hasLiked", interactionService.hasLiked(postId, userId)
        );
    }
}
