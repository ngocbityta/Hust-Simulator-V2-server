package com.hustsimulator.social.interaction;

import java.util.UUID;

/**
 * Data Transfer Objects for Like operations.
 */
public class InteractionDTO {

    public record LikeRequest(UUID postId) {}
}
