package com.hustsimulator.context.building;

import com.hustsimulator.context.entity.Building;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {
    List<Building> findByMapId(UUID mapId);
    List<Building> findByIsActiveTrue();
}
