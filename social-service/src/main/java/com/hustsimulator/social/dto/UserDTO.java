package com.hustsimulator.social.dto;

import java.util.UUID;

public record UserDTO(
    UUID id,
    String phonenumber,
    String username,
    String avatar,
    String coverImage,
    String description,
    String role,
    String status,
    Boolean online
) {}
