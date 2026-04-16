package com.hustsimulator.social.interaction;

import com.hustsimulator.social.entity.Like;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class InteractionServiceImpl implements InteractionService {

    private final InteractionRepository interactionRepository;

    @Override
    @Transactional
    public void likePost(UUID postId, UUID userId) {
        if (!interactionRepository.existsByUserIdAndPostId(userId, postId)) {
            log.info("User {} liked post {}", userId, postId);
            Like like = Like.builder()
                    .userId(userId)
                    .postId(postId)
                    .build();
            interactionRepository.save(like);
        }
    }

    @Override
    @Transactional
    public void unlikePost(UUID postId, UUID userId) {
        interactionRepository.findByUserIdAndPostId(userId, postId)
                .ifPresent(like -> {
                    log.info("User {} unliked post {}", userId, postId);
                    interactionRepository.delete(like);
                });
    }

    @Override
    public long countLikes(UUID postId) {
        return interactionRepository.countByPostId(postId);
    }

    @Override
    public boolean hasLiked(UUID postId, UUID userId) {
        return interactionRepository.existsByUserIdAndPostId(userId, postId);
    }
}
