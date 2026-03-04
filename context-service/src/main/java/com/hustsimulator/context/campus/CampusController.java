package com.hustsimulator.context.campus;

import com.hustsimulator.context.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CampusController {

    private final CampusService campusService;

    // --- Campus Zones ---

    @GetMapping("/campus-zones")
    public List<CampusZone> findAllZones() {
        return campusService.findAllZones();
    }

    @GetMapping("/campus-zones/active")
    public List<CampusZone> findActiveZones() {
        return campusService.findActiveZones();
    }

    @GetMapping("/campus-zones/{id}")
    public CampusZone findZoneById(@PathVariable UUID id) {
        return campusService.findZoneById(id);
    }

    @PostMapping("/campus-zones")
    @ResponseStatus(HttpStatus.CREATED)
    public CampusZone createZone(@Valid @RequestBody CampusZone zone) {
        return campusService.createZone(zone);
    }

    @PutMapping("/campus-zones/{id}")
    public CampusZone updateZone(@PathVariable UUID id, @Valid @RequestBody CampusZone zone) {
        return campusService.updateZone(id, zone);
    }

    @DeleteMapping("/campus-zones/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteZone(@PathVariable UUID id) {
        campusService.deleteZone(id);
    }

    // --- Player States ---

    @GetMapping("/player-states/user/{userId}")
    public PlayerState findPlayerState(@PathVariable UUID userId) {
        return campusService.findPlayerState(userId);
    }

    @GetMapping("/player-states/activity/{state}")
    public List<PlayerState> findByActivityState(@PathVariable PlayerActivityState state) {
        return campusService.findByActivityState(state);
    }

    @GetMapping("/player-states/zone/{zoneId}")
    public List<PlayerState> findByZoneId(@PathVariable UUID zoneId) {
        return campusService.findByZoneId(zoneId);
    }

    @PutMapping("/player-states/user/{userId}")
    public PlayerState updatePlayerState(
            @PathVariable UUID userId,
            @RequestParam PlayerActivityState state,
            @RequestParam(required = false) UUID zoneId,
            @RequestParam(required = false) String sessionData) {
        return campusService.updatePlayerState(userId, state, zoneId, sessionData);
    }
}
