package com.hustsimulator.context.recurringevent;

import com.hustsimulator.context.entity.RecurringEvent;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recurring-events")
@RequiredArgsConstructor
public class RecurringEventController {

    private final RecurringEventService recurringEventService;

    @GetMapping
    public List<RecurringEvent> findAll() {
        return recurringEventService.findAll();
    }

    @GetMapping("/active")
    public List<RecurringEvent> findActive() {
        return recurringEventService.findActive();
    }

    @GetMapping("/{id}")
    public RecurringEvent findById(@PathVariable UUID id) {
        return recurringEventService.findById(id);
    }

    @GetMapping("/map/{mapId}")
    public List<RecurringEvent> findByMapId(@PathVariable UUID mapId) {
        return recurringEventService.findByMapId(mapId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RecurringEvent create(@Valid @RequestBody RecurringEvent recurringEvent) {
        return recurringEventService.create(recurringEvent);
    }

    @PutMapping("/{id}")
    public RecurringEvent update(@PathVariable UUID id, @Valid @RequestBody RecurringEvent recurringEvent) {
        return recurringEventService.update(id, recurringEvent);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        recurringEventService.delete(id);
    }
}
