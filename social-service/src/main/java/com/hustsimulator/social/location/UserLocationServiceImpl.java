package com.hustsimulator.social.location;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserLocationServiceImpl implements UserLocationService {

    private final UserLocationRepository userLocationRepository;

    @Override
    @Transactional
    public void saveLocations(UUID userId, LocationDTO.SaveLocationsRequest request) {
        if (request.points() == null || request.points().isEmpty()) {
            return;
        }

        List<UserLocation> locations = request.points().stream()
                .map(point -> UserLocation.builder()
                        .userId(userId)
                        .latitude(point.latitude())
                        .longitude(point.longitude())
                        .timestamp(point.timestamp() != null ? point.timestamp() : LocalDateTime.now())
                        .build())
                .collect(Collectors.toList());

        userLocationRepository.saveAll(locations);
        log.debug("Saved {} locations for user {}", locations.size(), userId);
    }

    @Override
    @Transactional
    @Scheduled(cron = "0 0 * * * *") // Run at minute 0 past every hour
    public void cleanupOldLocations() {
        LocalDateTime threshold = LocalDateTime.now().minusDays(3);
        log.info("Cleaning up user locations older than {}", threshold);
        userLocationRepository.deleteOlderThan(threshold);
        log.info("Cleanup of old user locations completed.");
    }
}
