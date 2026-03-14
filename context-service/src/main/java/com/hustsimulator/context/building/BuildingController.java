package com.hustsimulator.context.building;

import com.hustsimulator.context.entity.Building;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    public List<Building> findAll() {
        return buildingService.findAll();
    }

    @GetMapping("/active")
    public List<Building> findActive() {
        return buildingService.findActive();
    }

    @GetMapping("/map/{mapId}")
    public List<Building> findByMapId(@PathVariable UUID mapId) {
        return buildingService.findByMapId(mapId);
    }

    @GetMapping("/{id}")
    public Building findById(@PathVariable UUID id) {
        return buildingService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Building create(@Valid @RequestBody BuildingService.CreateBuildingRequest request) {
        return buildingService.create(request);
    }

    @PutMapping("/{id}")
    public Building update(@PathVariable UUID id,
                           @Valid @RequestBody BuildingService.UpdateBuildingRequest request) {
        return buildingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        buildingService.delete(id);
    }

    @GetMapping("/{id}/contains")
    public boolean isPointInside(@PathVariable UUID id,
                                  @RequestParam double x,
                                  @RequestParam double y) {
        return buildingService.isPointInsideBuilding(id, x, y);
    }
}
