package com.hustsimulator.social.location;

import java.util.UUID;

public interface UserLocationService {
    void saveLocations(UUID userId, LocationDTO.SaveLocationsRequest request);
    void cleanupOldLocations();
}
