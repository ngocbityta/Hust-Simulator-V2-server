package com.hustsimulator.messaging.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Bảng bản sao (cache) thông tin User từ Auth Service.
 * Được cập nhật tự động qua RabbitMQ events.
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
    private String avatar;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt;
}
