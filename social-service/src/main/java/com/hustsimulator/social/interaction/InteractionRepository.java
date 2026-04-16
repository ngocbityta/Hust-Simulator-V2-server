package com.hustsimulator.social.interaction;

import com.hustsimulator.social.entity.Like;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InteractionRepository extends JpaRepository<Like, UUID> {
    Optional<Like> findByUserIdAndPostId(UUID userId, UUID postId);
    long countByPostId(UUID postId);
    boolean existsByUserIdAndPostId(UUID userId, UUID postId);
}
