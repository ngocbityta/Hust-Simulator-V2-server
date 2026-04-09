package com.hustsimulator.context.building;

import com.hustsimulator.context.entity.Building;
import java.util.List;
import java.util.UUID;

public interface BuildingService {
    List<Building> findAll();
    List<Building> findByMapId(UUID mapId);
    List<Building> findActive();
    Building findById(UUID id);
    Building create(CreateBuildingRequest request);
    Building update(UUID id, UpdateBuildingRequest request);
    void delete(UUID id);
    boolean isPointInsideBuilding(UUID buildingId, double x, double y);

    record CreateBuildingRequest(String name, UUID mapId, List<double[]> points) {}
    record UpdateBuildingRequest(String name, Boolean isActive) {}
}
