package com.hustsimulator.context.campusgraph;

import com.hustsimulator.context.entity.CampusEdge;
import com.hustsimulator.context.entity.CampusNode;
import com.hustsimulator.context.entity.Building;
import com.hustsimulator.context.building.BuildingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CampusGraphService {

    private final CampusNodeRepository nodeRepository;
    private final CampusEdgeRepository edgeRepository;
    private final BuildingRepository buildingRepository;

    @Cacheable("campus_graph")
    public CampusGraphDTO.GraphResponse getFullGraph() {
        List<CampusNode> nodes = nodeRepository.findByIsActiveTrue();
        List<CampusEdge> edges = edgeRepository.findAll();

        List<CampusGraphDTO.NodeResponse> nodeResponses = nodes.stream()
                .map(n -> new CampusGraphDTO.NodeResponse(
                        n.getId(), n.getName(), n.getNodeType(),
                        n.getLatitude(), n.getLongitude(), n.getBuildingId()))
                .collect(Collectors.toList());

        List<CampusGraphDTO.EdgeResponse> edgeResponses = edges.stream()
                .map(e -> new CampusGraphDTO.EdgeResponse(
                        e.getId(), e.getFromNodeId(), e.getToNodeId(),
                        e.getDistanceMeters(), e.getIsBidirectional()))
                .collect(Collectors.toList());

        return new CampusGraphDTO.GraphResponse(nodeResponses, edgeResponses);
    }

    @Cacheable("campus_full_graph")
    public CampusGraphDTO.FullGraphResponse getFullGraphWithBuildings() {
        CampusGraphDTO.GraphResponse graph = getFullGraph();

        List<CampusGraphDTO.BuildingPOI> buildings = buildingRepository.findByIsActiveTrue().stream()
                .filter(b -> b.getCentroidLat() != null && b.getCentroidLng() != null)
                .map(b -> new CampusGraphDTO.BuildingPOI(
                        b.getId(), b.getName(), b.getCentroidLat(), b.getCentroidLng()))
                .collect(Collectors.toList());

        return new CampusGraphDTO.FullGraphResponse(graph.nodes(), graph.edges(), buildings);
    }

    @Cacheable(value = "campus_nodes_by_type", key = "#nodeType")
    public List<CampusGraphDTO.NodeResponse> getNodesByType(String nodeType) {
        return nodeRepository.findByNodeType(nodeType).stream()
                .map(n -> new CampusGraphDTO.NodeResponse(
                        n.getId(), n.getName(), n.getNodeType(),
                        n.getLatitude(), n.getLongitude(), n.getBuildingId()))
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"campus_graph", "campus_nodes_by_type"}, allEntries = true)
    public CampusGraphDTO.NodeResponse createNode(CampusGraphDTO.CreateNodeRequest request) {
        CampusNode node = CampusNode.builder()
                .name(request.name())
                .nodeType(request.nodeType())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .buildingId(request.buildingId())
                .build();
        node = nodeRepository.save(node);
        log.info("Created campus node '{}' (type={})", node.getName(), node.getNodeType());
        return new CampusGraphDTO.NodeResponse(
                node.getId(), node.getName(), node.getNodeType(),
                node.getLatitude(), node.getLongitude(), node.getBuildingId());
    }

    @CacheEvict(value = {"campus_graph"}, allEntries = true)
    public CampusGraphDTO.EdgeResponse createEdge(CampusGraphDTO.CreateEdgeRequest request) {
        CampusNode fromNode = nodeRepository.findById(request.fromNodeId())
                .orElseThrow(() -> new RuntimeException("From node not found: " + request.fromNodeId()));
        CampusNode toNode = nodeRepository.findById(request.toNodeId())
                .orElseThrow(() -> new RuntimeException("To node not found: " + request.toNodeId()));

        double distance = haversine(fromNode.getLatitude(), fromNode.getLongitude(),
                toNode.getLatitude(), toNode.getLongitude());

        CampusEdge edge = CampusEdge.builder()
                .fromNodeId(request.fromNodeId())
                .toNodeId(request.toNodeId())
                .distanceMeters(distance)
                .isBidirectional(request.isBidirectional() != null ? request.isBidirectional() : true)
                .build();
        edge = edgeRepository.save(edge);
        log.info("Created campus edge {} -> {} ({}m)", fromNode.getName(), toNode.getName(),
                String.format("%.1f", distance));
        return new CampusGraphDTO.EdgeResponse(
                edge.getId(), edge.getFromNodeId(), edge.getToNodeId(),
                edge.getDistanceMeters(), edge.getIsBidirectional());
    }

    @CacheEvict(value = {"campus_graph", "campus_nodes_by_type"}, allEntries = true)
    public void deleteNode(UUID id) {
        nodeRepository.deleteById(id);
        log.info("Deleted campus node: {}", id);
    }

    @CacheEvict(value = {"campus_graph"}, allEntries = true)
    public void deleteEdge(UUID id) {
        edgeRepository.deleteById(id);
        log.info("Deleted campus edge: {}", id);
    }

    private double haversine(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371000;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }
}
