package com.hustsimulator.social.dto;

import java.util.UUID;

public record MapDTO(
    UUID id,
    String name,
    String coordinates,
    Boolean isActive
) {}
