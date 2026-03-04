package com.hustsimulator.context.campus;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.CampusZone;
import com.hustsimulator.context.entity.PlayerActivityState;
import com.hustsimulator.context.entity.PlayerState;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CampusServiceTest {

    @Mock private CampusZoneRepository campusZoneRepository;
    @Mock private PlayerStateRepository playerStateRepository;
    @InjectMocks private CampusService campusService;

    @Test
    void findAllZones_shouldReturnAll() {
        CampusZone zone = CampusZone.builder().name("Library").type("BUILDING").build();
        when(campusZoneRepository.findAll()).thenReturn(List.of(zone));

        List<CampusZone> result = campusService.findAllZones();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Library");
    }

    @Test
    void findZoneById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(campusZoneRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> campusService.findZoneById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void findPlayerState_shouldReturn() {
        UUID userId = UUID.randomUUID();
        PlayerState state = PlayerState.builder().userId(userId).activityState(PlayerActivityState.ROAMING).build();
        when(playerStateRepository.findByUserId(userId)).thenReturn(Optional.of(state));

        PlayerState result = campusService.findPlayerState(userId);

        assertThat(result.getActivityState()).isEqualTo(PlayerActivityState.ROAMING);
    }

    @Test
    void updatePlayerState_shouldCreateNew_whenNotExists() {
        UUID userId = UUID.randomUUID();
        UUID zoneId = UUID.randomUUID();
        when(playerStateRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(playerStateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlayerState result = campusService.updatePlayerState(userId, PlayerActivityState.IN_VIRTUAL_CLASS, zoneId, null);

        assertThat(result.getActivityState()).isEqualTo(PlayerActivityState.IN_VIRTUAL_CLASS);
        assertThat(result.getZoneId()).isEqualTo(zoneId);
        verify(playerStateRepository).save(any());
    }

    @Test
    void updatePlayerState_shouldUpdateExisting() {
        UUID userId = UUID.randomUUID();
        PlayerState existing = PlayerState.builder().userId(userId).activityState(PlayerActivityState.ROAMING).build();
        when(playerStateRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(playerStateRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        PlayerState result = campusService.updatePlayerState(userId, PlayerActivityState.IN_EVENT, null, "{\"eventId\":\"123\"}");

        assertThat(result.getActivityState()).isEqualTo(PlayerActivityState.IN_EVENT);
        assertThat(result.getSessionData()).contains("eventId");
    }
}
