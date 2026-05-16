package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.AdminBannerCreateRequest;
import com.example.my_movie_app.dto.AdminBannerDto;
import com.example.my_movie_app.dto.AdminBannerUpdateRequest;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.service.AdminBannerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/banners")
@RequiredArgsConstructor
public class AdminBannerController {

    private final AdminBannerService adminBannerService;

    @GetMapping
    public AdminPaginatedResponse<AdminBannerDto> getBanners(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "MOVIE") String actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminBannerService.getBanners(search, actionType, page, size);
    }

    @GetMapping("/{id}")
    public AdminBannerDto getBannerById(@PathVariable UUID id) {
        return adminBannerService.getBannerById(id);
    }

    // Cập nhật (Hứng Multipart)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminBannerDto updateBanner(
            @PathVariable UUID id,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart("data") AdminBannerUpdateRequest request
    ) {
        return adminBannerService.updateBanner(id, request, image);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminBannerDto createBanner(
            @RequestPart("image") MultipartFile image,
            @RequestPart("data") AdminBannerCreateRequest request
    ) {
        return adminBannerService.createBanner(request, image);
    }
}