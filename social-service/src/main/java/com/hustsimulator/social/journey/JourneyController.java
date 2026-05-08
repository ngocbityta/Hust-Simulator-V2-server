package com.hustsimulator.social.journey;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/journeys")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;

    @GetMapping("/today/preview")
    public JourneyDTO.JourneyResponse getPreviewForToday(@RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return journeyService.getPreviewForToday(userId);
    }

    @GetMapping("/user/{targetUserId}")
    public Page<JourneyDTO.JourneyResponse> getUserJourneys(
            @PathVariable UUID targetUserId,
            Pageable pageable) {
        // Here we ideally check privacy settings if targetUserId != requesterId
        return journeyService.getUserJourneys(targetUserId, pageable);
    }

    @GetMapping("/{id}")
    public JourneyDTO.JourneyResponse getJourneyById(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return journeyService.getJourneyById(id, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JourneyDTO.JourneyResponse createJourney(
            @Valid @RequestBody JourneyDTO.CreateJourneyRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return journeyService.createJourney(request, userId);
    }

    @PutMapping("/{id}")
    public JourneyDTO.JourneyResponse updateJourney(
            @PathVariable UUID id,
            @Valid @RequestBody JourneyDTO.UpdateJourneyRequest request,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return journeyService.updateJourney(id, request, userId);
    }

    @PostMapping("/{id}/publish")
    public JourneyDTO.JourneyResponse publishJourney(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return journeyService.publishJourney(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJourney(
            @PathVariable UUID id,
            @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        journeyService.deleteJourney(id, userId);
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
