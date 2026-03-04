package com.hustsimulator.context.search;

import com.hustsimulator.context.entity.*;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    public List<SearchHistory> findByUserId(UUID userId) {
        return searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public SearchHistory create(SearchHistory search) {
        return searchHistoryRepository.save(search);
    }

    public void delete(UUID id) {
        searchHistoryRepository.deleteById(id);
    }

    public void deleteAllByUserId(UUID userId) {
        List<SearchHistory> entries = searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId);
        searchHistoryRepository.deleteAll(entries);
    }
}
