package com.hustsimulator.context.room;

import com.hustsimulator.context.entity.Room;
import com.hustsimulator.context.enums.RoomStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface RoomRepository extends JpaRepository<Room, UUID> {
    List<Room> findByBuildingId(UUID buildingId);
    List<Room> findByStatusNot(RoomStatus status);
    Page<Room> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT r.buildingId, r.status, COUNT(r) FROM Room r GROUP BY r.buildingId, r.status")
    List<Object[]> countRoomStatusByBuilding();
}
