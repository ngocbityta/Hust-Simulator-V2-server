package com.hustsimulator.context.map;

import com.hustsimulator.context.entity.Map;
import com.hustsimulator.context.common.ResourceNotFoundException;
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
class MapServiceTest {

    @Mock private MapRepository mapRepository;
    @InjectMocks private MapServiceImpl mapService;

    @Test
    void findAllMaps_shouldReturnAll() {
        Map map = Map.builder().name("Map1").build();
        when(mapRepository.findAll()).thenReturn(List.of(map));

        List<Map> result = mapService.findAllMaps();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Map1");
    }

    @Test
    void findMapById_shouldThrow_whenNotFound() {
        UUID id = UUID.randomUUID();
        when(mapRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mapService.findMapById(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
