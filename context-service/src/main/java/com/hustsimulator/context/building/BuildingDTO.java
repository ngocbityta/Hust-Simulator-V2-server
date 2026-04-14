package com.hustsimulator.context.building;

import java.util.List;
import java.util.UUID;

/**
 * Data Transfer Objects for Building operations.
 */
public class BuildingDTO {

    public record CreateBuildingRequest(String name, UUID mapId, List<double[]> points) {}

    public record UpdateBuildingRequest(String name, Boolean isActive) {}
}
