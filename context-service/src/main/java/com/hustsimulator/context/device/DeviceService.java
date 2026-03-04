package com.hustsimulator.context.device;

import com.hustsimulator.context.entity.*;

import com.hustsimulator.context.common.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DeviceService {

    private final DeviceRepository deviceRepository;
    private final PushSettingRepository pushSettingRepository;

    // --- Devices ---

    public List<Device> findDevicesByUserId(UUID userId) {
        return deviceRepository.findByUserId(userId);
    }

    public Device addDevice(Device device) {
        return deviceRepository.save(device);
    }

    public void removeDevice(UUID deviceId) {
        deviceRepository.deleteById(deviceId);
    }

    // --- Push Settings ---

    public PushSetting findPushSetting(UUID userId) {
        return pushSettingRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("PushSetting", userId));
    }

    public PushSetting savePushSetting(PushSetting setting) {
        return pushSettingRepository.save(setting);
    }
}
