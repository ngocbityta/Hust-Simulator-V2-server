package com.hustsimulator.context.room;

import com.hustsimulator.context.entity.Room;
import java.util.List;
import java.util.UUID;

public interface RoomService {
    List<Room> findAll();
    List<Room> findByBuildingId(UUID buildingId);
    List<Room> findActive();
    Room findById(UUID id);
    Room create(RoomDTO.CreateRoomRequest request);
    Room update(UUID id, RoomDTO.UpdateRoomRequest request);
    void delete(UUID id);
}
