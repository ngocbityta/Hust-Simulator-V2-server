package com.hustsimulator.context.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsDTO getStats(@org.springframework.web.bind.annotation.RequestParam(defaultValue = "1d") String timeRange) {
        return dashboardService.getStats(timeRange);
    }
}
