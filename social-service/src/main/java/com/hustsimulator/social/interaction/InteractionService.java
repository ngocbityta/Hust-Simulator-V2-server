package com.hustsimulator.social.interaction;

import java.util.UUID;

public interface InteractionService {
    void likePost(UUID postId, UUID userId);
    void unlikePost(UUID postId, UUID userId);
    long countLikes(UUID postId);
    boolean hasLiked(UUID postId, UUID userId);
}
