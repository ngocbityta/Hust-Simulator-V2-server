package com.hustsimulator.context.issue;

import com.hustsimulator.context.entity.FacilityIssue;
import com.hustsimulator.context.enums.IssueStatus;
import com.hustsimulator.context.common.PageResponse;

import java.util.UUID;

public interface FacilityIssueService {
    FacilityIssue createIssue(FacilityIssueDTO.CreateIssueRequest request);
    
    FacilityIssue updateStatus(UUID issueId, FacilityIssueDTO.UpdateIssueStatusRequest request);
    
    PageResponse<FacilityIssue> getIssuesPaged(UUID buildingId, UUID roomId, IssueStatus status, int page, int size, String sort);
    
    int countOpenIssues(UUID buildingId);
    
    int countOpenRoomIssues(UUID roomId);
}
