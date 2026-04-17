package com.hustsimulator.auth.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO chứa thông tin User được publish lên RabbitMQ.
 * Chỉ chứa các trường cần thiết cho các service khác, KHÔNG chứa password hay token.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEvent implements Serializable {

    public enum EventType {
        CREATED, UPDATED, DELETED
    }

    private EventType eventType;
    private UUID userId;
    private String username;
    private String phonenumber;
    private String avatar;
    private String coverImage;
    private String description;
    private String role;
}
