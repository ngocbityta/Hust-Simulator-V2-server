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

    @Query(value = "SELECT hour_bucket, CAST(AVG(total_count) AS BIGINT) FROM (SELECT date_trunc('hour', h.recorded_at) AS hour_bucket, date_trunc('minute', h.recorded_at) AS minute_bucket, SUM(h.average_count) AS total_count FROM context.heatmap_history h WHERE h.recorded_at >= :since GROUP BY hour_bucket, minute_bucket) sub GROUP BY hour_bucket ORDER BY hour_bucket", nativeQuery = true)
    List<Object[]> findHourlyDensitySince(@org.springframework.data.repository.query.Param("since") LocalDateTime since);

    @Query(value = "SELECT day_bucket, CAST(AVG(total_count) AS BIGINT) FROM (SELECT date_trunc('day', h.recorded_at) AS day_bucket, date_trunc('minute', h.recorded_at) AS minute_bucket, SUM(h.average_count) AS total_count FROM context.heatmap_history h WHERE h.recorded_at >= :since GROUP BY day_bucket, minute_bucket) sub GROUP BY day_bucket ORDER BY day_bucket", nativeQuery = true)
    List<Object[]> findDailyDensitySince(@org.springframework.data.repository.query.Param("since") LocalDateTime since);

    @Query(value = "SELECT month_bucket, CAST(AVG(total_count) AS BIGINT) FROM (SELECT date_trunc('month', h.recorded_at) AS month_bucket, date_trunc('minute', h.recorded_at) AS minute_bucket, SUM(h.average_count) AS total_count FROM context.heatmap_history h WHERE h.recorded_at >= :since GROUP BY month_bucket, minute_bucket) sub GROUP BY month_bucket ORDER BY month_bucket", nativeQuery = true)
    List<Object[]> findMonthlyDensitySince(@org.springframework.data.repository.query.Param("since") LocalDateTime since);

    @Query(value = "SELECT h.recorded_at, h.average_count FROM context.heatmap_history h WHERE h.cell_x = :cellX AND h.cell_y = :cellY AND h.recorded_at >= :since ORDER BY h.average_count DESC LIMIT 1", nativeQuery = true)
    List<Object[]> findPeakDensityForCellSince(@org.springframework.data.repository.query.Param("cellX") Integer cellX, @org.springframework.data.repository.query.Param("cellY") Integer cellY, @org.springframework.data.repository.query.Param("since") LocalDateTime since);

    @org.springframework.data.jpa.repository.Query(value = "SELECT DISTINCT ON (h.cell_x, h.cell_y) h.cell_x, h.cell_y, h.recorded_at, h.average_count FROM context.heatmap_history h WHERE h.recorded_at >= :since AND (h.cell_x, h.cell_y) IN (:cells) ORDER BY h.cell_x, h.cell_y, h.average_count DESC", nativeQuery = true)
    List<Object[]> findPeakDensityForCellsSinceRaw(@org.springframework.data.repository.query.Param("cells") List<int[]> cells, @org.springframework.data.repository.query.Param("since") LocalDateTime since);

    default Map<String, Object[]> findPeakDensityForCellsSince(List<int[]> cells, LocalDateTime since) {
        if (cells.isEmpty()) return java.util.Collections.emptyMap();
        List<Object[]> rows = findPeakDensityForCellsSinceRaw(cells, since);
        Map<String, Object[]> result = new java.util.HashMap<>();
        for (Object[] row : rows) {
            String key = row[0] + "," + row[1];
            Object[] peakData = new Object[]{row[2], row[3]};
            result.put(key, peakData);
        }
        return result;
    }
}
