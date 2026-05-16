package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.AdminBookingDetailDto;
import com.example.my_movie_app.dto.AdminBookingDto;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.service.AdminBookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
public class AdminBookingController {

    private final AdminBookingService adminBookingService;

    @GetMapping
    public AdminPaginatedResponse<AdminBookingDto> getBookings(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "PAID") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminBookingService.getBookings(search, status, page, size);
    }

    @GetMapping("/{id}")
    public AdminBookingDetailDto getBookingDetail(@PathVariable UUID id) {
        return adminBookingService.getBookingDetail(id);
    }
}