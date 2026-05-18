package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.RevenueSummaryDto;
import com.example.my_movie_app.service.AdminRevenueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/revenue")
@RequiredArgsConstructor
public class AdminRevenueController {

    private final AdminRevenueService adminRevenueService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RevenueSummaryDto> getRevenueSummary(
            @RequestParam(defaultValue = "WEEK") String timeRange,
            @RequestParam(required = false) String targetDate // 🔥 Thêm param này
    ) {
        return ResponseEntity.ok(adminRevenueService.getRevenueSummary(timeRange, targetDate));
    }
}