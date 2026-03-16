package com.hustsimulator.context.building;

import com.hustsimulator.context.entity.Building;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/buildings")
@RequiredArgsConstructor
@Tag(name = "Building API", description = "Management of physical buildings and spatial checks")
public class BuildingController {

    private final BuildingService buildingService;

    @GetMapping
    @Operation(summary = "Find all buildings", description = "Retrieve a list of all buildings in the system")
    public List<Building> findAll() {
        return buildingService.findAll();
    }

    @GetMapping("/active")
    @Operation(summary = "Find all active buildings", description = "Retrieve a list of buildings currently active on maps")
    public List<Building> findActive() {
        return buildingService.findActive();
    }

    @GetMapping("/map/{mapId}")
    @Operation(summary = "Find buildings by Map ID", description = "Retrieve all buildings associated with a specific virtual map")
    public List<Building> findByMapId(@PathVariable UUID mapId) {
        return buildingService.findByMapId(mapId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find building by ID", description = "Retrieve detailed information about a building")
    public Building findById(@PathVariable UUID id) {
        return buildingService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new building", description = "Create a physical building defined by a polygon of coordinates")
    public Building create(@Valid @RequestBody BuildingService.CreateBuildingRequest request) {
        return buildingService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update building", description = "Update metadata or status of an existing building")
    public Building update(@PathVariable UUID id,
                           @Valid @RequestBody BuildingService.UpdateBuildingRequest request) {
        return buildingService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete building", description = "Permanently remove a building from the system")
    public void delete(@PathVariable UUID id) {
        buildingService.delete(id);
    }

    @GetMapping("/{id}/contains")
    @Operation(summary = "Point-in-polygon check", description = "Verify if a specific coordinate is physically inside the building boundaries")
    public boolean isPointInside(@PathVariable UUID id,
                                  @RequestParam double x,
                                  @RequestParam double y) {
        return buildingService.isPointInsideBuilding(id, x, y);
    }
}
