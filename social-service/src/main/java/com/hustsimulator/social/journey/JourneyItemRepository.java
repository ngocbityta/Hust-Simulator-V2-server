package com.hustsimulator.social.journey;

import com.hustsimulator.social.entity.JourneyItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JourneyItemRepository extends JpaRepository<JourneyItem, UUID> {
    List<JourneyItem> findByJourneyIdOrderBySortOrderAsc(UUID journeyId);
}
