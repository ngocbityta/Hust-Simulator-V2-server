package com.hustsimulator.social.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng bản sao (cache) thông tin User từ Auth Service.
 * Được cập nhật tự động qua RabbitMQ events, giúp Social Service
 * tra cứu tên/avatar tác giả bài Post mà KHÔNG cần gọi HTTP sang Auth Service.
 */
@Entity
@Table(name = "user_cache")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserCache {

    @Id
    private UUID id;

    private String username;
    private String phonenumber;
    private String avatar;

    @Column(name = "cover_image")
    private String coverImage;

    private String description;
    private String role;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
