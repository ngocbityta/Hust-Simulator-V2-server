package com.hustsimulator.social.location;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface UserLocationRepository extends JpaRepository<UserLocation, UUID> {
    
    List<UserLocation> findByUserIdAndTimestampBetweenOrderByTimestampAsc(UUID userId, LocalDateTime start, LocalDateTime end);

    @Modifying
    @Query("DELETE FROM UserLocation l WHERE l.timestamp < :threshold")
    void deleteOlderThan(@Param("threshold") LocalDateTime threshold);
}
