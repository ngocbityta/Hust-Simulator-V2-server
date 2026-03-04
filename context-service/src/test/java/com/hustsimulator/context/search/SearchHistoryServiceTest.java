package com.hustsimulator.context.search;

import com.hustsimulator.context.entity.SearchHistory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchHistoryServiceTest {

    @Mock private SearchHistoryRepository searchHistoryRepository;
    @InjectMocks private SearchHistoryService searchHistoryService;

    @Test
    void findByUserId_shouldReturnHistory() {
        UUID userId = UUID.randomUUID();
        SearchHistory entry = SearchHistory.builder().userId(userId).keyword("java").build();
        when(searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(List.of(entry));

        List<SearchHistory> result = searchHistoryService.findByUserId(userId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getKeyword()).isEqualTo("java");
    }

    @Test
    void create_shouldSave() {
        SearchHistory entry = SearchHistory.builder().userId(UUID.randomUUID()).keyword("spring").build();
        when(searchHistoryRepository.save(entry)).thenReturn(entry);

        SearchHistory result = searchHistoryService.create(entry);

        assertThat(result.getKeyword()).isEqualTo("spring");
    }

    @Test
    void deleteAllByUserId_shouldDeleteAll() {
        UUID userId = UUID.randomUUID();
        List<SearchHistory> entries = List.of(
                SearchHistory.builder().userId(userId).keyword("a").build(),
                SearchHistory.builder().userId(userId).keyword("b").build()
        );
        when(searchHistoryRepository.findByUserIdOrderByCreatedAtDesc(userId)).thenReturn(entries);

        searchHistoryService.deleteAllByUserId(userId);

        verify(searchHistoryRepository).deleteAll(entries);
    }
}
