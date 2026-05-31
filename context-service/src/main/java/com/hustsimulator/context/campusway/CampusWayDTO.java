package com.hustsimulator.context.campusway;

import java.util.List;
import java.util.UUID;

public record CampusWayDTO() {
    public record WayResponse(UUID id, String name, String wayType, List<List<Double>> coordinates, Double distanceMeters, Boolean isOneway) implements java.io.Serializable {}
    public record CreateWayRequest(String name, String wayType, List<List<Double>> coordinates, Boolean isOneway) {}
}
