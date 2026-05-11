package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.DashboardStatsDto;
import com.example.my_movie_app.dto.RevenuePointDto;
import com.example.my_movie_app.service.AdminDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;

@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class AdminDashboardController {

    private final AdminDashboardService dashboardService;

    @GetMapping("/stats")
    public DashboardStatsDto getStats() {
        return dashboardService.getDashboardStats();
    }

    @GetMapping("/revenue")
    public List<RevenuePointDto> getRevenueLast7Days() {
        return dashboardService.getRevenueLast7Days();
    }
}