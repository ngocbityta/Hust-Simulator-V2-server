package com.hustsimulator.context.device;

import com.hustsimulator.context.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DeviceController {

    private final DeviceService deviceService;

    // --- Devices ---

    @GetMapping("/devices/user/{userId}")
    public List<Device> findDevices(@PathVariable UUID userId) {
        return deviceService.findDevicesByUserId(userId);
    }

    @PostMapping("/devices")
    @ResponseStatus(HttpStatus.CREATED)
    public Device addDevice(@Valid @RequestBody Device device) {
        return deviceService.addDevice(device);
    }

    @DeleteMapping("/devices/{deviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeDevice(@PathVariable UUID deviceId) {
        deviceService.removeDevice(deviceId);
    }

    // --- Push Settings ---

    @GetMapping("/push-settings/{userId}")
    public PushSetting findPushSetting(@PathVariable UUID userId) {
        return deviceService.findPushSetting(userId);
    }

    @PutMapping("/push-settings/{userId}")
    public PushSetting savePushSetting(@PathVariable UUID userId, @Valid @RequestBody PushSetting setting) {
        setting.setUserId(userId);
        return deviceService.savePushSetting(setting);
    }
}
