package com.hustsimulator.context.room;

import com.hustsimulator.context.enums.RoomStatus;

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
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;

    @Override
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    public List<Room> findByBuildingId(UUID buildingId) {
        return roomRepository.findByBuildingId(buildingId);
    }

    @Override
    public List<Room> findActive() {
        return roomRepository.findByStatusNot(RoomStatus.CLOSED);
    }

    @Override
    public Room findById(UUID id) {
        return roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", id));
    }

    @Override
    public Room create(RoomDTO.CreateRoomRequest request) {
        log.info("Creating room '{}' in building {}", request.name(), request.buildingId());
        Room room = Room.builder()
                .name(request.name())
                .buildingId(request.buildingId())
                .build();
        return roomRepository.save(room);
    }

    @Override
    public Room update(UUID id, RoomDTO.UpdateRoomRequest request) {
        Room room = findById(id);
        if (request.name() != null) {
            room.setName(request.name());
        }
        log.info("Updating room: {}", id);
        return roomRepository.save(room);
    }

    @Override
    public void delete(UUID id) {
        Room room = findById(id);
        roomRepository.delete(room);
        log.info("Deleted room: {}", id);
    }

    @Override
    public com.hustsimulator.context.common.PageResponse<Room> getRoomsPaged(String search, int page, int size) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Room> roomPage;
        if (search != null && !search.trim().isEmpty()) {
            roomPage = roomRepository.findByNameContainingIgnoreCase(search.trim(), pageable);
        } else {
            roomPage = roomRepository.findAll(pageable);
        }
        return new com.hustsimulator.context.common.PageResponse<>(roomPage);
    }
}
