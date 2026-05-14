package com.hustsimulator.context.heatmap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hustsimulator.context.entity.HeatmapHistory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class HeatmapAnalyticsScheduler {

    private final StringRedisTemplate redisTemplate;
    private final HeatmapHistoryRepository repository;
    private final ObjectMapper objectMapper;

    // Run every 5 minutes for demonstration (in production this could be hourly)
    @Scheduled(fixedRate = 300000)
    @Transactional
    public void recordHistoricalDensity() {
        try {
            String latestHeatmapJson = redisTemplate.opsForValue().get("game:heatmap:latest");
            if (latestHeatmapJson == null) {
                log.debug("No latest heatmap data found in Redis.");
                return;
            }

            JsonNode rootNode = objectMapper.readTree(latestHeatmapJson);
            JsonNode cells = rootNode.get("cells");
            
            if (cells != null && cells.isArray()) {
                for (JsonNode cell : cells) {
                    Integer cellX = cell.get("cellX").asInt();
                    Integer cellY = cell.get("cellY").asInt();
                    Integer count = cell.get("count").asInt();

                    HeatmapHistory history = HeatmapHistory.builder()
                            .cellX(cellX)
                            .cellY(cellY)
                            .averageCount(count)
                            .build();
                            
                    repository.save(history);
                }
                log.info("Recorded historical density for {} cells.", cells.size());
            }

        } catch (Exception e) {
            log.error("Failed to record historical density: {}", e.getMessage(), e);
        }
    }
}
