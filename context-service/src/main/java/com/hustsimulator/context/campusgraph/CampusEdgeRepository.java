package com.hustsimulator.context.campusgraph;

import com.hustsimulator.context.entity.CampusEdge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CampusEdgeRepository extends JpaRepository<CampusEdge, UUID> {
    List<CampusEdge> findByFromNodeId(UUID fromNodeId);
    List<CampusEdge> findByToNodeId(UUID toNodeId);
    List<CampusEdge> findByFromNodeIdOrToNodeId(UUID fromNodeId, UUID toNodeId);
}
