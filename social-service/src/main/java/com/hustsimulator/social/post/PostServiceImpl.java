package com.hustsimulator.social.post;

import com.hustsimulator.social.common.GeometryUtils;
import com.hustsimulator.social.common.ResourceNotFoundException;
import com.hustsimulator.social.entity.Post;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import com.hustsimulator.social.enums.PostStatus;

@Service
@RequiredArgsConstructor
@Slf4j
public class PostServiceImpl implements PostService {

    private final PostRepository postRepository;

    @Override
    public Page<Post> findAll(Pageable pageable) {
        return postRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @Override
    public Page<Post> findByUserId(UUID userId, Pageable pageable) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable);
    }

    @Override
    public Page<Post> findByEventId(UUID eventId, Pageable pageable) {
        return postRepository.findByEventIdOrderByCreatedAtDesc(eventId, pageable);
    }

    @Override
    public Page<Post> findByBuildingId(UUID buildingId, Pageable pageable) {
        return postRepository.findByBuildingIdOrderByCreatedAtDesc(buildingId, pageable);
    }

    @Override
    public Page<Post> findByRoomId(UUID roomId, Pageable pageable) {
        return postRepository.findByRoomIdOrderByCreatedAtDesc(roomId, pageable);
    }

    @Override
    public Post findById(UUID id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post", id));
    }

    @Override
    @Transactional
    @CacheEvict(value = "journeyPreview", key = "#userId")
    public Post create(PostDTO.CreatePostRequest request, UUID userId) {
        log.info("Creating post for user {}", userId);
        Post post = Post.builder()
                .userId(userId)
                .content(request.content())
                .videoUrl(request.videoUrl())
                .location(GeometryUtils.createPoint(request.latitude(), request.longitude()))
                .eventId(request.eventId())
                .buildingId(request.buildingId())
                .roomId(request.roomId())
                .status(PostStatus.ACTIVE)
                .canEdit("1")
                .canComment("1")
                .banned("0")
                .build();
        return postRepository.save(post);
    }

    @Override
    @Transactional
    public Post update(UUID id, PostDTO.UpdatePostRequest request, UUID userId) {
        Post post = findById(id);
        
        if (!post.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to update this post");
        }

        if (request.content() != null) post.setContent(request.content());
        if (request.videoUrl() != null) post.setVideoUrl(request.videoUrl());
        if (request.status() != null) post.setStatus(request.status());
        if (request.canEdit() != null) post.setCanEdit(request.canEdit());
        if (request.canComment() != null) post.setCanComment(request.canComment());

        log.info("Updating post: {}", id);
        return postRepository.save(post);
    }

    @Override
    @Transactional
    @CacheEvict(value = "journeyPreview", key = "#userId")
    public void delete(UUID id, UUID userId) {
        Post post = findById(id);
        
        if (!post.getUserId().equals(userId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You are not allowed to delete this post");
        }

        postRepository.delete(post);
        log.info("Deleted post: {}", id);
    }
}
