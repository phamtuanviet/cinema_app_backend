package com.example.my_movie_app.controller;


// ...
import com.example.my_movie_app.dto.AdminCinemaCreateRequest;
import com.example.my_movie_app.dto.AdminCinemaDto;
import com.example.my_movie_app.dto.AdminCinemaUpdateRequest;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.service.AdminCinemaService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/cinemas")
@RequiredArgsConstructor
public class AdminCinemaController {

    private final AdminCinemaService adminCinemaService;

    @GetMapping
    public AdminPaginatedResponse<AdminCinemaDto> getCinemas(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminCinemaService.getCinemas(search, page, size);
    }

    @GetMapping("/regions")
    public List<String> getRegions() {
        return adminCinemaService.getAvailableRegions();
    }

    @GetMapping("/cineplexes")
    public List<String> getCineplexes() {
        return adminCinemaService.getAvailableCineplexes();
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminCinemaDto createCinema(
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart("data") AdminCinemaCreateRequest request
    ) {
        return adminCinemaService.createCinema(request, logo);
    }

    @GetMapping("/{id}")
    public AdminCinemaDto getCinemaById(@PathVariable UUID id) {
        return adminCinemaService.getCinemaById(id);
    }

    // Tương ứng với: @PUT("admin/cinemas/{id}") @Multipart
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminCinemaDto updateCinema(
            @PathVariable UUID id,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart("data") AdminCinemaUpdateRequest request
    ) {
        return adminCinemaService.updateCinema(id, request, logo);
    }
}