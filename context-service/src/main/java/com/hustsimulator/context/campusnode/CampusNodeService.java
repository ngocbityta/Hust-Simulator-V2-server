package com.hustsimulator.context.campusnode;

import com.hustsimulator.context.entity.CampusNode;
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
public class CampusNodeService {

    private final CampusNodeRepository nodeRepository;

    @Cacheable("campus_nodes_all")
    public List<CampusNodeDTO.NodeResponse> getAllNodes() {
        return nodeRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "campus_nodes_by_type", key = "#nodeType")
    public List<CampusNodeDTO.NodeResponse> getNodesByType(String nodeType) {
        return nodeRepository.findByNodeType(nodeType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"campus_nodes_all", "campus_nodes_by_type"}, allEntries = true)
    public CampusNodeDTO.NodeResponse createNode(CampusNodeDTO.CreateNodeRequest request) {
        CampusNode node = CampusNode.builder()
                .name(request.name())
                .nodeType(request.nodeType())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .buildingId(request.buildingId())
                .build();
        node = nodeRepository.save(node);
        log.info("Created campus node '{}' (type={})", node.getName(), node.getNodeType());
        return toResponse(node);
    }

    @CacheEvict(value = {"campus_nodes_all", "campus_nodes_by_type"}, allEntries = true)
    public void deleteNode(UUID id) {
        nodeRepository.deleteById(id);
        log.info("Deleted campus node: {}", id);
    }

    private CampusNodeDTO.NodeResponse toResponse(CampusNode node) {
        return new CampusNodeDTO.NodeResponse(
                node.getId(), node.getName(), node.getNodeType(),
                node.getLatitude(), node.getLongitude(), node.getBuildingId());
    }
}
