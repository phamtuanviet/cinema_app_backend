package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.dto.AdminShowtimeDto;
import com.example.my_movie_app.dto.AdminShowtimeUpdateRequest;
import com.example.my_movie_app.service.AdminShowtimeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/showtimes")
@RequiredArgsConstructor
public class AdminShowtimeController {

    private final AdminShowtimeService adminShowtimeService;

    @GetMapping
    public AdminPaginatedResponse<AdminShowtimeDto> getShowtimes(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "UPCOMING") String filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminShowtimeService.getShowtimes(search, filter, page, size);
    }

    @GetMapping("/{id}")
    public AdminShowtimeDto getShowtimeById(@PathVariable UUID id) {
        return adminShowtimeService.getShowtimeById(id);
    }

    // API Cập nhật (Đổi giờ hoặc Hủy)
    @PutMapping("/{id}")
    public AdminShowtimeDto updateShowtime(
            @PathVariable UUID id,
            @RequestBody AdminShowtimeUpdateRequest request
    ) {
        return adminShowtimeService.updateShowtime(id, request);
    }
}