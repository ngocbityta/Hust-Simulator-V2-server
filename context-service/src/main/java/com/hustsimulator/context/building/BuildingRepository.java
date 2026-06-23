package com.hustsimulator.context.building;

import com.hustsimulator.context.entity.Building;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BuildingRepository extends JpaRepository<Building, UUID> {
    List<Building> findByMapId(UUID mapId);
    List<Building> findByIsActiveTrue();
    @org.springframework.data.jpa.repository.Query("SELECT b FROM Building b WHERE :search IS NULL OR :search = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(b.id AS string) LIKE CONCAT('%', :search, '%')")
    Page<Building> findByNameOrIdContainingIgnoreCase(@org.springframework.data.repository.query.Param("search") String search, Pageable pageable);

    @org.springframework.data.jpa.repository.Query(value = "SELECT b, (SELECT COUNT(a) FROM BuildingAttendance a WHERE a.buildingId = b.id AND a.joinedAt >= :timestamp) as pop " +
           "FROM Building b WHERE (:search IS NULL OR :search = '' OR LOWER(b.name) LIKE LOWER(CONCAT('%', :search, '%')) OR CAST(b.id AS string) LIKE CONCAT('%', :search, '%'))")
    Page<Object[]> findBuildingsWithPopulation(@org.springframework.data.repository.query.Param("search") String search, @org.springframework.data.repository.query.Param("timestamp") java.time.LocalDateTime timestamp, Pageable pageable);
}
