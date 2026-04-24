package com.hustsimulator.social.dto;

import java.util.UUID;
import com.hustsimulator.social.enums.UserStatus;

public record UserDTO(
    UUID id,
    String phonenumber,
    String username,
    String avatar,
    String coverImage,
    String description,
    String role,
    UserStatus status,
    Boolean online
) {}
