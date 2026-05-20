package com.hustsimulator.social.follow;

import com.hustsimulator.social.entity.Follow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FollowRepository extends JpaRepository<Follow, UUID> {

    Optional<Follow> findByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    boolean existsByFollowerIdAndFollowingId(UUID followerId, UUID followingId);

    /** Users that this user is following */
    Page<Follow> findByFollowerIdOrderByCreatedAtDesc(UUID followerId, Pageable pageable);

    /** Users that follow this user */
    Page<Follow> findByFollowingIdOrderByCreatedAtDesc(UUID followingId, Pageable pageable);

    long countByFollowerId(UUID followerId);

    long countByFollowingId(UUID followingId);
}
