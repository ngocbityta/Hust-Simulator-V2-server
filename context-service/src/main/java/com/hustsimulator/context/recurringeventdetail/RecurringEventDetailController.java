package com.hustsimulator.context.recurringeventdetail;

import com.hustsimulator.context.entity.RecurringEventDetail;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/recurring-events")
@RequiredArgsConstructor
@Tag(name = "Recurring Event Details", description = "Manage individual occurrences of recurring events")
public class RecurringEventDetailController {

    private final RecurringEventDetailService detailService;

    @Operation(summary = "Get all occurrences for a specific recurring event")
    @GetMapping("/{id}/details")
    public List<RecurringEventDetail> findAllDetails(@PathVariable("id") UUID recurringEventId) {
        return detailService.findAll(recurringEventId);
    }

    @Operation(summary = "Get the currently active occurrence for a recurring event")
    @GetMapping("/{id}/details/current")
    public ResponseEntity<RecurringEventDetail> findCurrentDetail(@PathVariable("id") UUID recurringEventId) {
        return detailService.findCurrent(recurringEventId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @Operation(summary = "Get a specific recurring event detail by its ID")
    @GetMapping("/details/{detailId}")
    public RecurringEventDetail findById(@PathVariable UUID detailId) {
        return detailService.findById(detailId);
    }
}
