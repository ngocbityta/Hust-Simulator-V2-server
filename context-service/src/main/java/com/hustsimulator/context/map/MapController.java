package com.hustsimulator.context.map;

import com.hustsimulator.context.entity.Map;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
public class MapController {

    private final MapService mapService;

    @GetMapping
    public List<Map> findAllMaps() {
        return mapService.findAllMaps();
    }

    @GetMapping("/active")
    public List<Map> findActiveMaps() {
        return mapService.findActiveMaps();
    }

    @GetMapping("/{id}")
    public Map findMapById(@PathVariable UUID id) {
        return mapService.findMapById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Map createMap(@Valid @RequestBody Map map) {
        return mapService.createMap(map);
    }

    @PutMapping("/{id}")
    public Map updateMap(@PathVariable UUID id, @Valid @RequestBody Map map) {
        return mapService.updateMap(id, map);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMap(@PathVariable UUID id) {
        mapService.deleteMap(id);
    }
}
