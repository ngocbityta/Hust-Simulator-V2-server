package com.hustsimulator.context.heatmap;

import com.hustsimulator.context.entity.HeatmapHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HeatmapHistoryRepository extends JpaRepository<HeatmapHistory, UUID> {
    
    @Query("SELECT AVG(h.averageCount) FROM HeatmapHistory h WHERE h.cellX = :cellX AND h.cellY = :cellY AND h.recordedAt >= :since")
    Optional<Double> findAverageCountByCellSince(Integer cellX, Integer cellY, LocalDateTime since);

    @Query(value = "SELECT date_trunc('hour', h.recorded_at) AS hour_bucket, SUM(h.average_count) AS total_count FROM context.heatmap_history h WHERE h.recorded_at >= :since GROUP BY hour_bucket ORDER BY hour_bucket", nativeQuery = true)
    List<Object[]> findHourlyDensitySince(@org.springframework.data.repository.query.Param("since") LocalDateTime since);
}
