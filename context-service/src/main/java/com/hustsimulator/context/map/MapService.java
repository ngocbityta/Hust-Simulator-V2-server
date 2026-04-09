package com.hustsimulator.context.map;

import com.hustsimulator.context.entity.Map;
import java.util.List;
import java.util.UUID;

public interface MapService {
    List<Map> findAllMaps();
    List<Map> findActiveMaps();
    Map findMapById(UUID id);
    Map createMap(Map map);
    Map updateMap(UUID id, Map mapDetails);
    void deleteMap(UUID id);
    boolean isPointInsideMap(UUID mapId, double x, double y);
}
