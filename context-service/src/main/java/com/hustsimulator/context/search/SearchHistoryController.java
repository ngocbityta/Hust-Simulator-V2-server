package com.hustsimulator.context.search;

import com.hustsimulator.context.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/search-history")
@RequiredArgsConstructor
public class SearchHistoryController {

    private final SearchHistoryService searchHistoryService;

    @GetMapping("/user/{userId}")
    public List<SearchHistory> findByUserId(@PathVariable UUID userId) {
        return searchHistoryService.findByUserId(userId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SearchHistory create(@Valid @RequestBody SearchHistory search) {
        return searchHistoryService.create(search);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        searchHistoryService.delete(id);
    }

    @DeleteMapping("/user/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllByUserId(@PathVariable UUID userId) {
        searchHistoryService.deleteAllByUserId(userId);
    }
}
