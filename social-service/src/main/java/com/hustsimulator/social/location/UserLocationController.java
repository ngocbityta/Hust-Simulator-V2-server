package com.hustsimulator.social.location;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "User Location", description = "API for tracking user location over time (polling every 10s)")
public class UserLocationController {

    private final UserLocationService userLocationService;

    @PostMapping
    @Operation(summary = "Save user locations", description = "Client sends batched user locations (e.g. every 10 seconds). Locations older than 3 days are automatically purged by a cron job.")
    public ResponseEntity<Void> saveLocations(
            @RequestBody LocationDTO.SaveLocationsRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        
        UUID userId = resolveUserId(userIdHeader);
        userLocationService.saveLocations(userId, request);
        return ResponseEntity.ok().build();
    }

    private UUID resolveUserId(String userIdHeader) {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-User-Id header");
        }
        try {
            return UUID.fromString(userIdHeader);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid X-User-Id format");
        }
    }
}
