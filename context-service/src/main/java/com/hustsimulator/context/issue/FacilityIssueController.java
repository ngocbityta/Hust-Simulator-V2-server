package com.hustsimulator.context.issue;

import com.hustsimulator.context.common.PageResponse;
import com.hustsimulator.context.entity.FacilityIssue;
import com.hustsimulator.context.enums.IssueStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/issues")
@RequiredArgsConstructor
@Tag(name = "Facility Issues API", description = "Endpoints for reporting and managing facility issues")
public class FacilityIssueController {

    private final FacilityIssueService issueService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Report an issue", description = "Allows students to report a new facility issue")
    public FacilityIssue createIssue(@Valid @RequestBody FacilityIssueDTO.CreateIssueRequest request) {
        return issueService.createIssue(request);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update issue status", description = "Allows admins to mark issues as resolved")
    public FacilityIssue updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody FacilityIssueDTO.UpdateIssueStatusRequest request) {
        return issueService.updateStatus(id, request);
    }

    @GetMapping("/paged")
    @Operation(summary = "Get issues with pagination", description = "Retrieve a paginated list of facility issues with optional filters")
    public PageResponse<FacilityIssue> getIssuesPaged(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID buildingId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) IssueStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return issueService.getIssuesPaged(search, buildingId, roomId, status, page, size, sort);
    }
    
    @GetMapping("/building/{buildingId}/open-count")
    @Operation(summary = "Get open issue count", description = "Count how many unresolved issues a building has")
    public int getOpenIssueCount(@PathVariable UUID buildingId) {
        return issueService.countOpenIssues(buildingId);
    }
    
    @GetMapping("/room/{roomId}/open-count")
    @Operation(summary = "Get open room issue count", description = "Count how many unresolved issues a room has")
    public int getOpenRoomIssueCount(@PathVariable UUID roomId) {
        return issueService.countOpenRoomIssues(roomId);
    }
}
