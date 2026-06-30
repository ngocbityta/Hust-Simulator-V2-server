package com.hustsimulator.context.event;

import com.hustsimulator.context.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

import com.hustsimulator.context.enums.EventStatus;

public interface EventRepository extends JpaRepository<Event, UUID> {
    List<Event> findByStatusIn(List<EventStatus> statuses);
    List<Event> findByStatusInAndEndTimeAfter(List<EventStatus> statuses, java.time.LocalDateTime endTime);
    List<Event> findByStatus(EventStatus status);

    long countByStatus(EventStatus status);
    List<Event> findByMapId(UUID mapId);
    
    @org.springframework.data.jpa.repository.Query("SELECT e FROM Event e WHERE e.startTime <= :targetTime AND e.endTime >= :targetTime")
    List<Event> findActiveAt(@org.springframework.data.repository.query.Param("targetTime") java.time.LocalDateTime targetTime);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Event e WHERE e.createdAt IN (SELECT MIN(e2.createdAt) FROM Event e2 GROUP BY e2.name) AND (:search IS NULL OR :search = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(e.id AS string) LIKE CONCAT('%', :search, '%')) ORDER BY CASE e.status WHEN 'ONGOING' THEN 1 WHEN 'SCHEDULED' THEN 2 ELSE 3 END ASC, e.startTime ASC")
    Page<Event> findByNameOrIdContainingIgnoreCase(@org.springframework.data.repository.query.Param("search") String search, Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT e FROM Event e WHERE e.createdAt IN (SELECT MIN(e2.createdAt) FROM Event e2 GROUP BY e2.name) ORDER BY CASE e.status WHEN 'ONGOING' THEN 1 WHEN 'SCHEDULED' THEN 2 ELSE 3 END ASC, e.startTime ASC")
    Page<Event> findGroupedByName(Pageable pageable);

    @org.springframework.data.jpa.repository.Query("SELECT MIN(e.startTime), MAX(e.endTime) FROM Event e WHERE e.name = :name")
    List<Object[]> findMinMaxTimeByName(@org.springframework.data.repository.query.Param("name") String name);
}
