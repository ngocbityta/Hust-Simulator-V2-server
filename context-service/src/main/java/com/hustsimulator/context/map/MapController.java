package com.hustsimulator.context.map;

import com.hustsimulator.context.entity.Map;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/maps")
@RequiredArgsConstructor
@Tag(name = "Map API", description = "Management of virtual game maps and environments")
public class MapController {

    private final MapService mapService;

    @GetMapping
    @Operation(summary = "Find all maps", description = "Retrieve a list of all game maps defined in the system")
    public List<Map> findAllMaps() {
        return mapService.findAllMaps();
    }

    @GetMapping("/active")
    @Operation(summary = "Find active maps", description = "Retrieve a list of maps that are currently available for players")
    public List<Map> findActiveMaps() {
        return mapService.findActiveMaps();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find map by ID", description = "Retrieve metadata for a specific map")
    public Map findMapById(@PathVariable UUID id) {
        return mapService.findMapById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create map", description = "Register a new virtual map in the system")
    public Map createMap(@Valid @RequestBody Map map) {
        return mapService.createMap(map);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update map", description = "Modify metadata or availability of an existing map")
    public Map updateMap(@PathVariable UUID id, @Valid @RequestBody Map map) {
        return mapService.updateMap(id, map);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete map", description = "Remove a map from the system")
    public void deleteMap(@PathVariable UUID id) {
        mapService.deleteMap(id);
    }
}
