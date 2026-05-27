package com.hustsimulator.context.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flywaydb.core.api.output.MigrateResult;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class FlywayConfig {

    private final CacheManager cacheManager;
    private static boolean migrationsExecuted = false;

    @Bean
    public FlywayMigrationStrategy flywayMigrationStrategy() {
        return flyway -> {
            MigrateResult result = flyway.migrate();
            if (result != null && result.migrationsExecuted > 0) {
                log.info("Flyway executed {} migrations. Flagging for cache eviction on startup...", result.migrationsExecuted);
                migrationsExecuted = true;
            } else {
                log.info("No Flyway migrations executed. Caches remain intact.");
            }
        };
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        if (migrationsExecuted) {
            log.info("Application is ready. Evicting all application caches due to recent DB migrations...");
            try {
                cacheManager.getCacheNames().forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                        log.info("Cleared cache region: {}", cacheName);
                    }
                });
                log.info("Successfully cleared application caches to prevent stale data.");
                migrationsExecuted = false; // reset flag
            } catch (Exception e) {
                log.error("Failed to clear caches after migration", e);
            }
        }
    }
}
