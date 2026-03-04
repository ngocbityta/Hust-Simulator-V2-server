package com.hustsimulator.context.search;

import com.hustsimulator.context.entity.*;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface SearchHistoryRepository extends JpaRepository<SearchHistory, UUID> {

    List<SearchHistory> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
