package com.hustsimulator.context.heatmap;

import com.hustsimulator.context.entity.HeatmapHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HeatmapHistoryRepository extends JpaRepository<HeatmapHistory, UUID> {
    
    @Query("SELECT AVG(h.averageCount) FROM HeatmapHistory h WHERE h.cellX = :cellX AND h.cellY = :cellY AND h.recordedAt >= :since")
    Optional<Double> findAverageCountByCellSince(Integer cellX, Integer cellY, LocalDateTime since);
}
