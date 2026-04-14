package com.hustsimulator.context.room;

import com.hustsimulator.context.entity.Room;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public List<Room> findAll() {
        return roomService.findAll();
    }

    @GetMapping("/active")
    public List<Room> findActive() {
        return roomService.findActive();
    }

    @GetMapping("/building/{buildingId}")
    public List<Room> findByBuildingId(@PathVariable UUID buildingId) {
        return roomService.findByBuildingId(buildingId);
    }

    @GetMapping("/{id}")
    public Room findById(@PathVariable UUID id) {
        return roomService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Room create(@Valid @RequestBody RoomDTO.CreateRoomRequest request) {
        return roomService.create(request);
    }

    @PutMapping("/{id}")
    public Room update(@PathVariable UUID id,
                       @Valid @RequestBody RoomDTO.UpdateRoomRequest request) {
        return roomService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        roomService.delete(id);
    }
}
