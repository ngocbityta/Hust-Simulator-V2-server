package com.hustsimulator.context.device;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PushSettingRepository extends JpaRepository<PushSetting, UUID> {
}
