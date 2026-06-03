package com.hustsimulator.context.userstate;

import com.hustsimulator.context.entity.BuildingAttendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface BuildingAttendanceRepository extends JpaRepository<BuildingAttendance, UUID> {
    Optional<BuildingAttendance> findTopByUserIdAndBuildingIdAndLeftAtIsNullOrderByJoinedAtDesc(UUID userId, UUID buildingId);
}
