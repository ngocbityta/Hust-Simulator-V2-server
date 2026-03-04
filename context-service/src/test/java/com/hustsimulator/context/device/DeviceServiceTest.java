package com.hustsimulator.context.device;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Device;
import com.hustsimulator.context.entity.PushSetting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeviceServiceTest {

    @Mock private DeviceRepository deviceRepository;
    @Mock private PushSettingRepository pushSettingRepository;
    @InjectMocks private DeviceService deviceService;

    @Test
    void findDevicesByUserId_shouldReturnDevices() {
        UUID userId = UUID.randomUUID();
        Device device = Device.builder().userId(userId).devToken("token123").build();
        when(deviceRepository.findByUserId(userId)).thenReturn(List.of(device));

        List<Device> result = deviceService.findDevicesByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getDevToken()).isEqualTo("token123");
    }

    @Test
    void addDevice_shouldSave() {
        Device device = Device.builder().userId(UUID.randomUUID()).devToken("token").build();
        when(deviceRepository.save(device)).thenReturn(device);

        Device result = deviceService.addDevice(device);

        assertThat(result).isNotNull();
    }

    @Test
    void findPushSetting_shouldReturn() {
        UUID userId = UUID.randomUUID();
        PushSetting setting = PushSetting.builder().userId(userId).build();
        when(pushSettingRepository.findById(userId)).thenReturn(Optional.of(setting));

        PushSetting result = deviceService.findPushSetting(userId);

        assertThat(result.getUserId()).isEqualTo(userId);
    }

    @Test
    void findPushSetting_shouldThrow_whenNotFound() {
        UUID userId = UUID.randomUUID();
        when(pushSettingRepository.findById(userId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> deviceService.findPushSetting(userId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
