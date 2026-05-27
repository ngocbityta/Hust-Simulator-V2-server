package com.hustsimulator.context.campusgraph;

import com.hustsimulator.context.entity.CampusNode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CampusNodeRepository extends JpaRepository<CampusNode, UUID> {
    List<CampusNode> findByIsActiveTrue();
    List<CampusNode> findByNodeType(String nodeType);
    Optional<CampusNode> findByName(String name);
    Optional<CampusNode> findByBuildingId(UUID buildingId);
}
