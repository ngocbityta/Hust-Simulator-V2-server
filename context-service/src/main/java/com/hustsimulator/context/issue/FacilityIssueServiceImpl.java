package com.hustsimulator.context.issue;

import com.hustsimulator.context.common.PageResponse;
import com.hustsimulator.context.common.ResourceNotFoundException;
import com.hustsimulator.context.entity.FacilityIssue;
import com.hustsimulator.context.enums.IssueStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FacilityIssueServiceImpl implements FacilityIssueService {

    private final FacilityIssueRepository issueRepository;

    @Override
    public FacilityIssue createIssue(FacilityIssueDTO.CreateIssueRequest request) {
        log.info("Creating new facility issue for building {}", request.buildingId());
        FacilityIssue issue = FacilityIssue.builder()
                .buildingId(request.buildingId())
                .roomId(request.roomId())
                .reporterId(request.reporterId())
                .category(request.category())
                .description(request.description())
                .status(IssueStatus.OPEN)
                .build();
        return issueRepository.save(issue);
    }

    @Override
    public FacilityIssue updateStatus(UUID issueId, FacilityIssueDTO.UpdateIssueStatusRequest request) {
        FacilityIssue issue = issueRepository.findById(issueId)
                .orElseThrow(() -> new ResourceNotFoundException("FacilityIssue", issueId));
        
        issue.setStatus(request.status());
        if (request.status() == IssueStatus.RESOLVED) {
            issue.setResolvedBy(request.resolvedBy());
            issue.setResolvedAt(LocalDateTime.now());
        }
        
        log.info("Updated status of issue {} to {}", issueId, request.status());
        return issueRepository.save(issue);
    }

    @Override
    public PageResponse<FacilityIssue> getIssuesPaged(UUID buildingId, UUID roomId, IssueStatus status, int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        Sort.Direction direction = sortParams.length > 1 && sortParams[1].equalsIgnoreCase("asc") ? Sort.Direction.ASC : Sort.Direction.DESC;
        Sort sortObj = Sort.by(direction, sortParams[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<FacilityIssue> issuesPage = issueRepository.findIssuesWithFilters(buildingId, status, roomId, pageable);
        return new PageResponse<>(issuesPage);
    }

    @Override
    public int countOpenIssues(UUID buildingId) {
        return issueRepository.countByBuildingIdAndStatus(buildingId, IssueStatus.OPEN);
    }
    
    @Override
    public int countOpenRoomIssues(UUID roomId) {
        return issueRepository.countByRoomIdAndStatus(roomId, IssueStatus.OPEN);
    }
}
