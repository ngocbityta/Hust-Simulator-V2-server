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

    @org.springframework.data.jpa.repository.Query("SELECT r FROM Room r WHERE " +
           "(:buildingId IS NULL OR r.buildingId = :buildingId) AND " +
           "(:floorNum IS NULL OR r.floorNum = :floorNum) AND " +
           "(:type IS NULL OR :type = '' OR r.type = :type) AND " +
           "(:search IS NULL OR :search = '' OR LOWER(r.name) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Room> findRoomsWithFilters(
            @org.springframework.data.repository.query.Param("buildingId") UUID buildingId,
            @org.springframework.data.repository.query.Param("floorNum") Integer floorNum,
            @org.springframework.data.repository.query.Param("type") String type,
            @org.springframework.data.repository.query.Param("search") String search,
            Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT r.buildingId, r.status, COUNT(r) FROM Room r GROUP BY r.buildingId, r.status")
    List<Object[]> countRoomStatusByBuilding();
}
