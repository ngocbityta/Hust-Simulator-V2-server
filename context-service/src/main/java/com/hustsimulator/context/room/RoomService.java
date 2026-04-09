package com.hustsimulator.context.room;

import com.hustsimulator.context.entity.Room;
import java.util.List;
import java.util.UUID;

public interface RoomService {
    List<Room> findAll();
    List<Room> findByBuildingId(UUID buildingId);
    List<Room> findActive();
    Room findById(UUID id);
    Room create(CreateRoomRequest request);
    Room update(UUID id, UpdateRoomRequest request);
    void delete(UUID id);

    record CreateRoomRequest(String name, UUID buildingId) {}
    record UpdateRoomRequest(String name, Boolean isActive) {}
}
