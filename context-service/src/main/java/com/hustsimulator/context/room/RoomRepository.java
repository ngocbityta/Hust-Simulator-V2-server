package com.hustsimulator.context.room;

import com.hustsimulator.context.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByBuildingId(UUID buildingId);
    List<Room> findByIsActiveTrue();
}
