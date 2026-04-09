package com.hustsimulator.context.building;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Building;
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
class BuildingServiceTest {

    @Mock
    private BuildingRepository buildingRepository;
    
    private ObjectMapper objectMapper = new ObjectMapper();
    private BuildingServiceImpl buildingService;

    @BeforeEach
    void setUp() {
        buildingService = new BuildingServiceImpl(buildingRepository, objectMapper);
    }

    @Test
    void findById_shouldReturnBuilding_whenExists() {
        UUID id = UUID.randomUUID();
        Building building = Building.builder().name("D1").build();
        building.setId(id);
        
        when(buildingRepository.findById(id)).thenReturn(Optional.of(building));

        Building result = buildingService.findById(id);

        assertThat(result.getName()).isEqualTo("D1");
    }

    @Test
    void findById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(buildingRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> buildingService.findById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void create_shouldSaveAndReturn() {
        UUID mapId = UUID.randomUUID();
        List<double[]> points = List.of(new double[]{0, 0}, new double[]{10, 0}, new double[]{10, 10}, new double[]{0, 10});
        BuildingService.CreateBuildingRequest request = new BuildingService.CreateBuildingRequest("D6", mapId, points);

        when(buildingRepository.save(any(Building.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Building result = buildingService.create(request);

        assertThat(result.getName()).isEqualTo("D6");
        assertThat(result.getMapId()).isEqualTo(mapId);
        assertThat(result.getCoordinates()).contains("10.0");
        verify(buildingRepository).save(any(Building.class));
    }

    @Test
    void isPointInsideBuilding_shouldReturnTrue_whenInside() throws Exception {
        UUID id = UUID.randomUUID();
        // Square 0,0 to 10,10
        String coords = "[[0,0],[10,0],[10,10],[0,10]]";
        Building building = Building.builder().name("D1").coordinates(coords).build();
        building.setId(id);

        when(buildingRepository.findById(id)).thenReturn(Optional.of(building));

        // Inside
        assertThat(buildingService.isPointInsideBuilding(id, 5, 5)).isTrue();
        // Outside
        assertThat(buildingService.isPointInsideBuilding(id, 15, 15)).isFalse();
        // On edge (isPointInPolygon logic dependent, usually true or false depending on cross-number implementation)
        // Our GeometryUtils uses Ray Casting which usually handles edges.
        assertThat(buildingService.isPointInsideBuilding(id, 0, 0)).isTrue();
    }
}
