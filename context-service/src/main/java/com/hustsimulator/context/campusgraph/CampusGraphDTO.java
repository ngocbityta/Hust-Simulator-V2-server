package com.hustsimulator.context.campusgraph;

import java.util.List;
import java.util.UUID;

public record CampusGraphDTO() {

    public record NodeResponse(UUID id, String name, String nodeType, Double latitude, Double longitude, UUID buildingId) implements java.io.Serializable {}

    public record EdgeResponse(UUID id, UUID fromNodeId, UUID toNodeId, Double distanceMeters, Boolean isBidirectional) implements java.io.Serializable {}

    public record BuildingPOI(UUID id, String name, Double centroidLat, Double centroidLng) implements java.io.Serializable {}

    public record GraphResponse(List<NodeResponse> nodes, List<EdgeResponse> edges) implements java.io.Serializable {}

    public record FullGraphResponse(List<NodeResponse> nodes, List<EdgeResponse> edges, List<BuildingPOI> buildings) implements java.io.Serializable {}

    public record CreateNodeRequest(String name, String nodeType, Double latitude, Double longitude, UUID buildingId) {}

    public record CreateEdgeRequest(UUID fromNodeId, UUID toNodeId, Boolean isBidirectional) {}
}
