package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
@Tag(name = "Event API", description = "Management of temporary game events and activities")
public class EventController {

    private final EventService eventService;

    @GetMapping
    @Operation(summary = "Find all events", description = "Retrieve a list of all events in the system")
    public List<Event> findAll() {
        return eventService.findAll();
    }

    @GetMapping("/active")
    @Operation(summary = "Find active events", description = "Retrieve events that are currently happening")
    public List<Event> findActiveEvents() {
        return eventService.findActiveEvents();
    }

    @GetMapping("/map/{mapId}")
    @Operation(summary = "Find events by Map ID", description = "Retrieve all events scheduled for a specific map")
    public List<Event> findByMapId(@PathVariable UUID mapId) {
        return eventService.findByMapId(mapId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Find event by ID", description = "Retrieve detailed information about an event")
    public Event findById(@PathVariable UUID id) {
        return eventService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create event", description = "Register a new game event")
    public Event create(@Valid @RequestBody EventDTO.CreateEventRequest request) {
        return eventService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update event", description = "Modify details or status of an existing event")
    public Event update(@PathVariable UUID id, @Valid @RequestBody EventDTO.UpdateEventRequest request) {
        return eventService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete event", description = "Remove an event from the system")
    public void delete(@PathVariable UUID id) {
        eventService.delete(id);
    }
}
