package com.hustsimulator.context.campusway;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/campus-ways")
@RequiredArgsConstructor
public class CampusWayController {

    private final CampusWayService wayService;

    @GetMapping
    public ResponseEntity<List<CampusWayDTO.WayResponse>> getWays(
            @RequestParam(required = false) String type) {
        if (type != null) {
            return ResponseEntity.ok(wayService.getWaysByType(type.toUpperCase()));
        }
        return ResponseEntity.ok(wayService.getAllWays());
    }

    @PostMapping
    public ResponseEntity<CampusWayDTO.WayResponse> createWay(
            @RequestBody CampusWayDTO.CreateWayRequest request) {
        return ResponseEntity.ok(wayService.createWay(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWay(@PathVariable UUID id) {
        wayService.deleteWay(id);
        return ResponseEntity.noContent().build();
    }
}
