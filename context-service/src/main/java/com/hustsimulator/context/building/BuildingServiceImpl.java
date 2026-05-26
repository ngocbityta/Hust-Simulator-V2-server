package com.hustsimulator.context.building;

import com.hustsimulator.context.common.GeometryUtils;
import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Building;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingServiceImpl implements BuildingService {

    private final BuildingRepository buildingRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Cacheable("buildings_all")
    public List<Building> findAll() {
        return buildingRepository.findAll();
    }

    @Override
    @Cacheable(value = "buildings_by_map", key = "#mapId")
    public List<Building> findByMapId(UUID mapId) {
        return buildingRepository.findByMapId(mapId);
    }

    @Override
    @Cacheable("buildings_active")
    public List<Building> findActive() {
        return buildingRepository.findByIsActiveTrue();
    }

    @Override
    @Cacheable(value = "buildings", key = "#id")
    public Building findById(UUID id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", id));
    }

    @Override
    @CacheEvict(value = {"buildings_all", "buildings_active", "buildings_by_map"}, allEntries = true)
    public Building create(BuildingDTO.CreateBuildingRequest request) {
        log.info("Creating building '{}' on map {}", request.name(), request.mapId());

        String coordinatesJson = serializePoints(request.points());

        Building building = Building.builder()
                .name(request.name())
                .mapId(request.mapId())
                .coordinates(coordinatesJson)
                .build();

        return buildingRepository.save(building);
    }

    @Override
    @CacheEvict(value = {"buildings", "buildings_all", "buildings_active", "buildings_by_map"}, allEntries = true)
    public Building update(UUID id, BuildingDTO.UpdateBuildingRequest request) {
        Building building = findById(id);
        if (request.name() != null) {
            building.setName(request.name());
        }
        if (request.isActive() != null) {
            building.setIsActive(request.isActive());
        }
        log.info("Updating building: {}", id);
        return buildingRepository.save(building);
    }

    @Override
    @CacheEvict(value = {"buildings", "buildings_all", "buildings_active", "buildings_by_map"}, allEntries = true)
    public void delete(UUID id) {
        Building building = findById(id);
        buildingRepository.delete(building);
        log.info("Deleted building: {}", id);
    }

    @Override
    public com.hustsimulator.context.common.PageResponse<Building> getBuildingsPaged(String search, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Building> buildingPage;
        if (search != null && !search.trim().isEmpty()) {
            buildingPage = buildingRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            buildingPage = buildingRepository.findAll(pageable);
        }
        return new com.hustsimulator.context.common.PageResponse<>(buildingPage);
    }

    @Override
    public boolean isPointInsideBuilding(UUID buildingId, double x, double y) {
        Building building = findById(buildingId);
        return GeometryUtils.isPointInPolygonJson(building.getCoordinates(), x, y, objectMapper);
    }

    private String serializePoints(List<double[]> points) {
        try {
            return objectMapper.writeValueAsString(points);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize points", e);
        }
    }
}
