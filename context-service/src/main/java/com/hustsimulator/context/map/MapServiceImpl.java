package com.hustsimulator.context.map;

import com.hustsimulator.context.entity.Map;
import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.common.GeometryUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapServiceImpl implements MapService {

    private final MapRepository mapRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<Map> findAllMaps() {
        return mapRepository.findAll();
    }

    @Override
    public List<Map> findActiveMaps() {
        return mapRepository.findByIsActiveTrue();
    }

    @Override
    public Map findMapById(UUID id) {
        return mapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Map", id));
    }

    @Override
    public Map createMap(Map map) {
        log.info("Creating new virtual map: {}", map.getName());
        return mapRepository.save(map);
    }

    @Override
    public Map updateMap(UUID id, Map mapDetails) {
        Map map = findMapById(id);
        map.setName(mapDetails.getName());
        map.setCoordinates(mapDetails.getCoordinates());
        map.setIsActive(mapDetails.getIsActive());

        log.info("Updating virtual map: {}", id);
        return mapRepository.save(map);
    }

    @Override
    public void deleteMap(UUID id) {
        Map map = findMapById(id);
        mapRepository.delete(map);
        log.info("Deleted virtual map: {}", id);
    }

    @Override
    public boolean isPointInsideMap(UUID mapId, double x, double y) {
        Map map = findMapById(mapId);
        return GeometryUtils.isPointInPolygonJson(map.getCoordinates(), x, y, objectMapper);
    }
}
