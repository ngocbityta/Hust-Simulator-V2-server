package com.hustsimulator.social.journey;

import com.hustsimulator.social.entity.Journey;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JourneyRepository extends JpaRepository<Journey, UUID> {
    Page<Journey> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    Optional<Journey> findByUserIdAndJourneyDate(UUID userId, LocalDate journeyDate);
}
