package com.hustsimulator.context.post;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostRepository extends JpaRepository<Post, UUID> {

    List<Post> findByAuthorId(UUID authorId);

    List<Post> findByCourseId(UUID courseId);
}
