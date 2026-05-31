package com.hustsimulator.context.campusnode;

import java.util.UUID;

public record CampusNodeDTO() {
    public record NodeResponse(UUID id, String name, String nodeType, Double latitude, Double longitude, UUID buildingId) implements java.io.Serializable {}
    public record CreateNodeRequest(String name, String nodeType, Double latitude, Double longitude, UUID buildingId) {}
}
