package com.hustsimulator.social.usercache;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/**
 * DTO nhận từ RabbitMQ — phải khớp cấu trúc với UserEvent bên auth-service.
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
