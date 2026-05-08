package com.hustsimulator.social.post;

import com.hustsimulator.social.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PostRepository extends JpaRepository<Post, UUID> {
    Page<Post> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Page<Post> findByEventIdOrderByCreatedAtDesc(UUID eventId, Pageable pageable);
    Page<Post> findByBuildingIdOrderByCreatedAtDesc(UUID buildingId, Pageable pageable);
    Page<Post> findByRoomIdOrderByCreatedAtDesc(UUID roomId, Pageable pageable);
    Page<Post> findAllByOrderByCreatedAtDesc(Pageable pageable);
    java.util.List<Post> findByUserIdAndCreatedAtBetweenOrderByCreatedAtAsc(UUID userId, java.time.LocalDateTime start, java.time.LocalDateTime end);
}
