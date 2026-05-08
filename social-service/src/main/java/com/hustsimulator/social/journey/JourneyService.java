package com.hustsimulator.social.journey;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface JourneyService {
    JourneyDTO.JourneyResponse getPreviewForToday(UUID userId);
    Page<JourneyDTO.JourneyResponse> getUserJourneys(UUID userId, Pageable pageable);
    JourneyDTO.JourneyResponse getJourneyById(UUID id, UUID userId);
    JourneyDTO.JourneyResponse createJourney(JourneyDTO.CreateJourneyRequest request, UUID userId);
    JourneyDTO.JourneyResponse updateJourney(UUID id, JourneyDTO.UpdateJourneyRequest request, UUID userId);
    JourneyDTO.JourneyResponse publishJourney(UUID id, UUID userId);
    void deleteJourney(UUID id, UUID userId);
}
