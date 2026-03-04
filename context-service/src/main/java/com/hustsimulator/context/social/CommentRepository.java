package com.hustsimulator.context.social;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, UUID> {

    List<Comment> findByPostId(UUID postId);

    List<Comment> findByUserId(UUID userId);
}
