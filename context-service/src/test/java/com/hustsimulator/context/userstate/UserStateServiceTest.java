package com.hustsimulator.context.userstate;

import com.hustsimulator.context.building.BuildingService;
import com.hustsimulator.context.entity.UserState;
import com.hustsimulator.context.enums.UserActivityState;
import com.hustsimulator.context.map.MapService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserStateServiceTest {

    @Mock private UserStateRepository userStateRepository;
    @Mock private BuildingService buildingService;
    @Mock private MapService mapService;

    private UserStateServiceImpl userStateService;

    @BeforeEach
    void setUp() {
        userStateService = new UserStateServiceImpl(userStateRepository, buildingService, mapService);
    }

    @Test
    void findByUserId_shouldCreateDefault_whenNotExist() {
        UUID userId = UUID.randomUUID();
        when(userStateRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(userStateRepository.save(any(UserState.class))).thenAnswer(i -> i.getArgument(0));

        UserState result = userStateService.findByUserId(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getActivityState()).isEqualTo(UserActivityState.OUTSIDE_MAP);
    }

    @Test
    void joinBuilding_shouldSetSpectating_whenNotPhysicallyInside() {
        UUID userId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();
        UserState state = UserState.builder().userId(userId).build();
        
        when(userStateRepository.findByUserId(userId)).thenReturn(Optional.of(state));
        when(buildingService.isPointInsideBuilding(bId, 1.0, 1.0)).thenReturn(false);
        when(userStateRepository.save(any(UserState.class))).thenAnswer(i -> i.getArgument(0));

        UserState result = userStateService.joinBuilding(userId, bId, 1.0, 1.0);

        assertThat(result.getBuildingId()).isEqualTo(bId);
        assertThat(result.getActivityState()).isEqualTo(UserActivityState.SPECTATING_BUILDING);
    }

    @Test
    void joinBuilding_shouldSetInBuilding_whenPhysicallyInside() {
        UUID userId = UUID.randomUUID();
        UUID bId = UUID.randomUUID();
        UserState state = UserState.builder().userId(userId).build();

        when(userStateRepository.findByUserId(userId)).thenReturn(Optional.of(state));
        when(buildingService.isPointInsideBuilding(bId, 5.0, 5.0)).thenReturn(true);
        when(userStateRepository.save(any(UserState.class))).thenAnswer(i -> i.getArgument(0));

        UserState result = userStateService.joinBuilding(userId, bId, 5.0, 5.0);

        assertThat(result.getActivityState()).isEqualTo(UserActivityState.IN_BUILDING);
    }

    @Test
    void joinRoom_shouldThrow_whenNotInBuilding() {
        UUID userId = UUID.randomUUID();
        UUID rId = UUID.randomUUID();
        UserState state = UserState.builder().userId(userId).activityState(UserActivityState.ROAMING).build();

        when(userStateRepository.findByUserId(userId)).thenReturn(Optional.of(state));

        assertThatThrownBy(() -> userStateService.joinRoom(userId, rId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("User must be IN_BUILDING");
    }

    @Test
    void leaveBuilding_shouldSetRoaming() {
        UUID userId = UUID.randomUUID();
        UserState state = UserState.builder()
                .userId(userId)
                .buildingId(UUID.randomUUID())
                .activityState(UserActivityState.IN_BUILDING)
                .build();

        when(userStateRepository.findByUserId(userId)).thenReturn(Optional.of(state));
        when(userStateRepository.save(any(UserState.class))).thenAnswer(i -> i.getArgument(0));

        UserState result = userStateService.leaveBuilding(userId);

        assertThat(result.getBuildingId()).isNull();
        assertThat(result.getActivityState()).isEqualTo(UserActivityState.ROAMING);
    }

    @Test
    void syncPositionState_shouldSetOutsideMap_whenOutOfAllMaps() {
        UUID userId = UUID.randomUUID();
        UserState state = UserState.builder().userId(userId).activityState(UserActivityState.ROAMING).build();
        
        com.hustsimulator.context.entity.Map map1 = new com.hustsimulator.context.entity.Map();
        map1.setId(UUID.randomUUID());
        
        when(userStateRepository.findByUserId(userId)).thenReturn(Optional.of(state));
        when(mapService.findActiveMaps()).thenReturn(List.of(map1));
        when(mapService.isPointInsideMap(any(), anyDouble(), anyDouble())).thenReturn(false);
        when(userStateRepository.save(any(UserState.class))).thenAnswer(i -> i.getArgument(0));

        UserState result = userStateService.syncPositionState(userId, 99.0, 99.0);

        assertThat(result.getActivityState()).isEqualTo(UserActivityState.OUTSIDE_MAP);
        assertThat(result.getMapId()).isNull();
    }
}
