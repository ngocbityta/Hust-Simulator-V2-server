package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public List<Event> findAll() {
        return eventService.findAll();
    }

    @GetMapping("/active")
    public List<Event> findActiveEvents() {
        return eventService.findActiveEvents();
    }

    @GetMapping("/map/{mapId}")
    public List<Event> findByMapId(@PathVariable UUID mapId) {
        return eventService.findByMapId(mapId);
    }

    @GetMapping("/{id}")
    public Event findById(@PathVariable UUID id) {
        return eventService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Event create(@Valid @RequestBody Event event) {
        return eventService.create(event);
    }

    @PutMapping("/{id}")
    public Event update(@PathVariable UUID id, @Valid @RequestBody Event event) {
        return eventService.update(id, event);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        eventService.delete(id);
    }
}
