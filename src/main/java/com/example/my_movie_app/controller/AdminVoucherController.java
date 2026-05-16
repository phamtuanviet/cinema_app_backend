package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.*;
import com.example.my_movie_app.service.AdminVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/vouchers")
@RequiredArgsConstructor
public class AdminVoucherController {

    private final AdminVoucherService adminVoucherService;

    @GetMapping
    public AdminPaginatedResponse<AdminVoucherDto> getVouchers(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "VALID") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminVoucherService.getVouchers(search, status, page, size);
    }

    @GetMapping("/{id}")
    public AdminVoucherDto getVoucherById(@PathVariable UUID id) {
        return adminVoucherService.getVoucherById(id);
    }

    // Cập nhật Voucher
    @PutMapping("/{id}")
    public AdminVoucherDto updateVoucher(
            @PathVariable UUID id,
            @RequestBody AdminVoucherUpdateRequest request
    ) {
        return adminVoucherService.updateVoucher(id, request);
    }

    @PostMapping
    public AdminVoucherDto createVoucher(@RequestBody AdminVoucherCreateRequest request) {
        return adminVoucherService.createVoucher(request);
    }

    @GetMapping("/active-list")
    public List<AdminVoucherSimpleDto> getActiveVouchers() {
        return adminVoucherService.getActiveVouchers();
    }
}