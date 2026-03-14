package com.hustsimulator.context.map;

import com.hustsimulator.context.entity.Map;
import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class MapService {

    private final MapRepository mapRepository;

    public List<Map> findAllMaps() {
        return mapRepository.findAll();
    }

    public List<Map> findActiveMaps() {
        return mapRepository.findByIsActiveTrue();
    }

    public Map findMapById(UUID id) {
        return mapRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Map", id));
    }

    public Map createMap(Map map) {
        log.info("Creating new virtual map: {}", map.getName());
        return mapRepository.save(map);
    }

    public Map updateMap(UUID id, Map mapDetails) {
        Map map = findMapById(id);
        map.setName(mapDetails.getName());
        map.setType(mapDetails.getType());
        map.setRadius(mapDetails.getRadius());
        map.setMetadata(mapDetails.getMetadata());
        map.setIsActive(mapDetails.getIsActive());

        log.info("Updating virtual map: {}", id);
        return mapRepository.save(map);
    }

    public void deleteMap(UUID id) {
        Map map = findMapById(id);
        mapRepository.delete(map);
        log.info("Deleted virtual map: {}", id);
    }
}
