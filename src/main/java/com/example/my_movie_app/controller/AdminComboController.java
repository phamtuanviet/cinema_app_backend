package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.AdminComboCreateRequest;
import com.example.my_movie_app.dto.AdminComboDto;
import com.example.my_movie_app.dto.AdminComboUpdateRequest;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.service.AdminComboService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/combos")
@RequiredArgsConstructor
public class AdminComboController {

    private final AdminComboService adminComboService;

    @GetMapping
    public AdminPaginatedResponse<AdminComboDto> getCombos(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "true") Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminComboService.getCombos(search, isActive, page, size);
    }

    @GetMapping("/{id}")
    public AdminComboDto getComboById(@PathVariable UUID id) {
        return adminComboService.getComboById(id);
    }

    // Cập nhật Combo (Hứng Multipart)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminComboDto createCombo(
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart("data") AdminComboCreateRequest request
    ) {
        return adminComboService.createCombo(request, image);
    }

    // API UPDATE (Đã làm ở bước trước)
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminComboDto updateCombo(
            @PathVariable UUID id,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestPart("data") AdminComboUpdateRequest request
    ) {
        return adminComboService.updateCombo(id, request, image);
    }
}