package com.hustsimulator.social.journey;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Parameter;

import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/journeys")
@RequiredArgsConstructor
public class JourneyController {

    private final JourneyService journeyService;

    @GetMapping("/generate")
    public JourneyDTO.JourneyResponse generateJourneyDraft(
            @RequestParam(required = false) LocalDateTime startTime,
            @RequestParam(required = false) LocalDateTime endTime,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        if (startTime == null) startTime = LocalDate.now().atStartOfDay();
        if (endTime == null) endTime = LocalDate.now().atTime(LocalTime.MAX);
        return journeyService.generateJourneyDraft(userId, startTime, endTime);
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
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return journeyService.getJourneyById(id, userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public JourneyDTO.JourneyResponse createJourney(
            @Valid @RequestBody JourneyDTO.CreateJourneyRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return journeyService.createJourney(request, userId);
    }

    @PutMapping("/{id}")
    public JourneyDTO.JourneyResponse updateJourney(
            @PathVariable UUID id,
            @Valid @RequestBody JourneyDTO.UpdateJourneyRequest request,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return journeyService.updateJourney(id, request, userId);
    }

    @PostMapping("/{id}/publish")
    public JourneyDTO.JourneyResponse publishJourney(
            @PathVariable UUID id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
        UUID userId = resolveUserId(userIdHeader);
        return journeyService.publishJourney(id, userId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteJourney(
            @PathVariable UUID id,
            @Parameter(hidden = true) @RequestHeader("X-User-Id") String userIdHeader) {
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
