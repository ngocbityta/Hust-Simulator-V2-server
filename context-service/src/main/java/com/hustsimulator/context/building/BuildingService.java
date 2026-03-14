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

    /**
     * Create a building from a list of clockwise-ordered points.
     * The service will split the polygon into convex sub-polygons (triangles)
     * using JTS PolygonTriangulator and store both the original coordinates
     * and the resulting convex polygons as JSON.
     */
    public Building create(CreateBuildingRequest request) {
        log.info("Creating building '{}' on map {}", request.name(), request.mapId());

        // Parse user-provided points into a JTS Polygon
        List<double[]> points = request.points().stream()
                .map(p -> new double[]{p.x(), p.y()})
                .collect(Collectors.toList());

        Polygon polygon = GeometryUtils.createPolygon(points);

        // Split into convex polygons (triangles)
        List<Polygon> convexPolygons = GeometryUtils.splitIntoConvexPolygons(polygon);

        // Serialize
        String originalJson = serializePoints(request.points());
        String convexJson = serializeConvexPolygons(convexPolygons);

        Building building = Building.builder()
                .name(request.name())
                .mapId(request.mapId())
                .originalCoordinates(originalJson)
                .convexPolygons(convexJson)
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

    /**
     * Check if a point (x, y) is inside the building's convex polygons.
     */
    public boolean isPointInsideBuilding(UUID buildingId, double x, double y) {
        Building building = findById(buildingId);
        List<Polygon> polygons = deserializeConvexPolygons(building.getConvexPolygons());
        return GeometryUtils.isPointInsideAnyPolygon(x, y, polygons);
    }

    // --- JSON serialization helpers ---

    private String serializePoints(List<PointDto> points) {
        try {
            return objectMapper.writeValueAsString(points);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize points", e);
        }
    }

    private String serializeConvexPolygons(List<Polygon> polygons) {
        try {
            List<List<PointDto>> result = polygons.stream()
                    .map(poly -> {
                        var coords = poly.getCoordinates();
                        List<PointDto> pts = new java.util.ArrayList<>();
                        for (var coord : coords) {
                            pts.add(new PointDto(coord.getX(), coord.getY()));
                        }
                        return pts;
                    })
                    .collect(Collectors.toList());
            return objectMapper.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize convex polygons", e);
        }
    }

    public List<Polygon> deserializeConvexPolygons(String json) {
        try {
            List<List<PointDto>> raw = objectMapper.readValue(json,
                    new TypeReference<List<List<PointDto>>>() {});
            return raw.stream()
                    .map(pts -> {
                        List<double[]> coords = pts.stream()
                                .map(p -> new double[]{p.x(), p.y()})
                                .collect(Collectors.toList());
                        return GeometryUtils.createPolygon(coords);
                    })
                    .collect(Collectors.toList());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to deserialize convex polygons", e);
        }
    }

    // --- DTOs ---

    public record PointDto(double x, double y) {}
    public record CreateBuildingRequest(String name, UUID mapId, List<PointDto> points) {}
    public record UpdateBuildingRequest(String name, Boolean isActive) {}
}
