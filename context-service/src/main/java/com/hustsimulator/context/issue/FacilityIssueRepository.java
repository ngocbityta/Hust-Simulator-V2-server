package com.hustsimulator.context.issue;

import com.hustsimulator.context.entity.FacilityIssue;
import com.hustsimulator.context.enums.IssueStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface FacilityIssueRepository extends JpaRepository<FacilityIssue, UUID> {
    
    @Query("SELECT i FROM FacilityIssue i WHERE " +
           "(:buildingId IS NULL OR i.buildingId = :buildingId) AND " +
           "(:status IS NULL OR i.status = :status) AND " +
           "(:roomId IS NULL OR i.roomId = :roomId)")
    Page<FacilityIssue> findIssuesWithFilters(
            @Param("buildingId") UUID buildingId,
            @Param("status") IssueStatus status,
            @Param("roomId") UUID roomId,
            Pageable pageable);
            
    int countByBuildingIdAndStatus(UUID buildingId, IssueStatus status);
    
    int countByRoomIdAndStatus(UUID roomId, IssueStatus status);
}
