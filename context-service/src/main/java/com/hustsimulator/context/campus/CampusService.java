package com.hustsimulator.context.campus;

import com.hustsimulator.context.entity.*;

import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CampusService {

    private final CampusZoneRepository campusZoneRepository;
    private final PlayerStateRepository playerStateRepository;

    // --- CampusZone ---

    public List<CampusZone> findAllZones() {
        return campusZoneRepository.findAll();
    }

    public List<CampusZone> findActiveZones() {
        return campusZoneRepository.findByIsActiveTrue();
    }

    public CampusZone findZoneById(UUID id) {
        return campusZoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("CampusZone", id));
    }

    public CampusZone createZone(CampusZone zone) {
        return campusZoneRepository.save(zone);
    }

    public CampusZone updateZone(UUID id, CampusZone updated) {
        CampusZone zone = findZoneById(id);
        zone.setName(updated.getName());
        zone.setType(updated.getType());
        zone.setRadius(updated.getRadius());
        zone.setMetadata(updated.getMetadata());
        zone.setIsActive(updated.getIsActive());
        return campusZoneRepository.save(zone);
    }

    public void deleteZone(UUID id) {
        campusZoneRepository.deleteById(id);
    }

    // --- PlayerState ---

    public PlayerState findPlayerState(UUID userId) {
        return playerStateRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("PlayerState", userId));
    }

    public List<PlayerState> findByActivityState(PlayerActivityState state) {
        return playerStateRepository.findByActivityState(state);
    }

    public List<PlayerState> findByZoneId(UUID zoneId) {
        return playerStateRepository.findByZoneId(zoneId);
    }

    public PlayerState updatePlayerState(UUID userId, PlayerActivityState state, UUID zoneId, String sessionData) {
        PlayerState playerState = playerStateRepository.findByUserId(userId)
                .orElseGet(() -> PlayerState.builder().userId(userId).build());
        playerState.setActivityState(state);
        playerState.setZoneId(zoneId);
        playerState.setSessionData(sessionData != null ? sessionData : "{}");
        playerState.setEnteredAt(LocalDateTime.now());
        return playerStateRepository.save(playerState);
    }
}
