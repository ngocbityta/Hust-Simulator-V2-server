package com.hustsimulator.context.building;

import com.hustsimulator.context.common.GeometryUtils;
import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Building;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BuildingService {

    private final BuildingRepository buildingRepository;
    private final ObjectMapper objectMapper;

    public List<Building> findAll() {
        return buildingRepository.findAll();
    }

    public List<Building> findByMapId(UUID mapId) {
        return buildingRepository.findByMapId(mapId);
    }

    public List<Building> findActive() {
        return buildingRepository.findByIsActiveTrue();
    }

    public Building findById(UUID id) {
        return buildingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Building", id));
    }

    public Building create(CreateBuildingRequest request) {
        log.info("Creating building '{}' on map {}", request.name(), request.mapId());

        String coordinatesJson = serializePoints(request.points());

        Building building = Building.builder()
                .name(request.name())
                .mapId(request.mapId())
                .coordinates(coordinatesJson)
                .build();

        return buildingRepository.save(building);
    }

    public Building update(UUID id, UpdateBuildingRequest request) {
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

    public void delete(UUID id) {
        Building building = findById(id);
        buildingRepository.delete(building);
        log.info("Deleted building: {}", id);
    }

    public boolean isPointInsideBuilding(UUID buildingId, double x, double y) {
        Building building = findById(buildingId);
        Polygon polygon = deserializePointsToPolygon(building.getCoordinates());
        return GeometryUtils.isPointInsideAnyPolygon(x, y, List.of(polygon));
    }

    // --- JSON serialization helpers ---

    private String serializePoints(List<PointDto> points) {
        try {
            return objectMapper.writeValueAsString(points);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize points", e);
        }
    }

    public Polygon deserializePointsToPolygon(String json) {
        try {
            List<PointDto> pts = objectMapper.readValue(json,
                    new TypeReference<List<PointDto>>() {});
            List<double[]> coords = pts.stream()
                    .map(p -> new double[]{p.x(), p.y()})
                    .collect(Collectors.toList());
            return GeometryUtils.createPolygon(coords);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize coordinates", e);
        }
    }

    // --- DTOs ---

    public record PointDto(double x, double y) {}
    public record CreateBuildingRequest(String name, UUID mapId, List<PointDto> points) {}
    public record UpdateBuildingRequest(String name, Boolean isActive) {}
}
