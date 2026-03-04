package com.hustsimulator.context.social;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PostLikeRepository extends JpaRepository<PostLike, UUID> {

    List<PostLike> findByPostId(UUID postId);

    Optional<PostLike> findByUserIdAndPostId(UUID userId, UUID postId);

    long countByPostId(UUID postId);
}
