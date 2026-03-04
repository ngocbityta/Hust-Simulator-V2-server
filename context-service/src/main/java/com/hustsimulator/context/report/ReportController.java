package com.hustsimulator.context.report;

import com.hustsimulator.context.entity.*;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public List<Report> findAll() {
        return reportService.findAll();
    }

    @GetMapping("/{id}")
    public Report findById(@PathVariable UUID id) {
        return reportService.findById(id);
    }

    @GetMapping("/post/{postId}")
    public List<Report> findByPostId(@PathVariable UUID postId) {
        return reportService.findByPostId(postId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Report create(@Valid @RequestBody Report report) {
        return reportService.create(report);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        reportService.delete(id);
    }
}
