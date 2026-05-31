package com.hustsimulator.context.campusway;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hustsimulator.context.entity.CampusWay;
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
public class CampusWayService {

    private final CampusWayRepository wayRepository;
    private final ObjectMapper objectMapper;

    @Cacheable("campus_ways_all")
    public List<CampusWayDTO.WayResponse> getAllWays() {
        return wayRepository.findByIsActiveTrue().stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Cacheable(value = "campus_ways_by_type", key = "#wayType")
    public List<CampusWayDTO.WayResponse> getWaysByType(String wayType) {
        return wayRepository.findByWayType(wayType).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @CacheEvict(value = {"campus_ways_all", "campus_ways_by_type"}, allEntries = true)
    public CampusWayDTO.WayResponse createWay(CampusWayDTO.CreateWayRequest request) {
        String coordsJson;
        try {
            coordsJson = objectMapper.writeValueAsString(request.coordinates());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid coordinates format", e);
        }

        double totalDistance = calculatePolylineDistance(request.coordinates());

        CampusWay way = CampusWay.builder()
                .name(request.name())
                .wayType(request.wayType())
                .coordinates(coordsJson)
                .distanceMeters(totalDistance)
                .isOneway(request.isOneway() != null ? request.isOneway() : false)
                .build();
        way = wayRepository.save(way);
        log.info("Created campus way '{}' (type={}, {}m)",
                way.getName(), way.getWayType(), String.format("%.1f", totalDistance));
        return toResponse(way);
    }

    @CacheEvict(value = {"campus_ways_all", "campus_ways_by_type"}, allEntries = true)
    public void deleteWay(UUID id) {
        wayRepository.deleteById(id);
        log.info("Deleted campus way: {}", id);
    }

    private CampusWayDTO.WayResponse toResponse(CampusWay way) {
        List<List<Double>> coords;
        try {
            coords = objectMapper.readValue(way.getCoordinates(),
                    new TypeReference<List<List<Double>>>() {});
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse coordinates for way '{}': {}", way.getName(), e.getMessage());
            coords = List.of();
        }
        return new CampusWayDTO.WayResponse(
                way.getId(), way.getName(), way.getWayType(),
                coords, way.getDistanceMeters(), way.getIsOneway());
    }

    private double calculatePolylineDistance(List<List<Double>> coordinates) {
        double total = 0;
        for (int i = 0; i < coordinates.size() - 1; i++) {
            List<Double> from = coordinates.get(i);
            List<Double> to = coordinates.get(i + 1);
            total += haversine(from.get(1), from.get(0), to.get(1), to.get(0));
        }
        return total;
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
