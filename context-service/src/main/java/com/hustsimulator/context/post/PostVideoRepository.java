package com.hustsimulator.context.post;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PostVideoRepository extends JpaRepository<PostVideo, UUID> {

    List<PostVideo> findByPostId(UUID postId);
}
