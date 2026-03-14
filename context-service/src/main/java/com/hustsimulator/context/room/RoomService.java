package com.hustsimulator.context.room;

import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomService {

    private final RoomRepository roomRepository;

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public List<Room> findByBuildingId(UUID buildingId) {
        return roomRepository.findByBuildingId(buildingId);
    }

    public List<Room> findActive() {
        return roomRepository.findByIsActiveTrue();
    }

    public Room findById(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    public Room create(CreateRoomRequest request) {
        log.info("Creating room '{}' in building {}", request.name(), request.buildingId());
        Room room = Room.builder()
                .name(request.name())
                .buildingId(request.buildingId())
                .build();
        return roomRepository.save(room);
    }

    public Room update(UUID id, UpdateRoomRequest request) {
        Room room = findById(id);
        if (request.name() != null) {
            room.setName(request.name());
        }
        if (request.isActive() != null) {
            room.setIsActive(request.isActive());
        }
        log.info("Updating room: {}", id);
        return roomRepository.save(room);
    }

    public void delete(UUID id) {
        Room room = findById(id);
        roomRepository.delete(room);
        log.info("Deleted room: {}", id);
    }

    public record CreateRoomRequest(String name, UUID buildingId) {}
    public record UpdateRoomRequest(String name, Boolean isActive) {}
}
