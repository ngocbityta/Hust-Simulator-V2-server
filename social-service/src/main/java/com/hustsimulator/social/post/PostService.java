package com.hustsimulator.social.post;

import com.hustsimulator.social.entity.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PostService {
    Page<Post> findAll(Pageable pageable);
    Page<Post> findByUserId(UUID userId, Pageable pageable);
    Page<Post> findByEventId(UUID eventId, Pageable pageable);
    Page<Post> findByBuildingId(UUID buildingId, Pageable pageable);
    Page<Post> findByRoomId(UUID roomId, Pageable pageable);
    Post findById(UUID id);
    Post create(PostDTO.CreatePostRequest request, UUID userId);
    Post update(UUID id, PostDTO.UpdatePostRequest request, UUID userId);
    void delete(UUID id, UUID userId);
}
