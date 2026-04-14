package com.hustsimulator.context.building;

import com.hustsimulator.context.entity.Building;
import java.util.List;
import java.util.UUID;

public interface BuildingService {
    List<Building> findAll();
    List<Building> findByMapId(UUID mapId);
    List<Building> findActive();
    Building findById(UUID id);
    Building create(BuildingDTO.CreateBuildingRequest request);
    Building update(UUID id, BuildingDTO.UpdateBuildingRequest request);
    void delete(UUID id);
    boolean isPointInsideBuilding(UUID buildingId, double x, double y);
}
