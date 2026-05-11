package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.AdminMovieCreateRequest;
import com.example.my_movie_app.dto.AdminMovieDto;
import com.example.my_movie_app.dto.AdminMovieUpdateRequest;
import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.service.AdminMovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/api/admin/movies")
@RequiredArgsConstructor
public class AdminMovieController {

    private final AdminMovieService adminMovieService;

    @GetMapping
    public AdminPaginatedResponse<AdminMovieDto> getMovies(
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return adminMovieService.getMovies(search, page, size);
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminMovieDto createMovie(
            @RequestPart(value = "poster", required = false) MultipartFile poster,
            @RequestPart("data") AdminMovieCreateRequest request
    ) {
        return adminMovieService.createMovie(request, poster);
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public AdminMovieDto updateMovie(
            @PathVariable UUID id,
            @RequestPart(value = "poster", required = false) MultipartFile poster,
            @RequestPart("data") AdminMovieUpdateRequest request
    ) {
        return adminMovieService.updateMovie(id, request, poster);
    }

    @GetMapping("/{id}")
    public AdminMovieDto getMovieById(@PathVariable UUID id) {
        return adminMovieService.getMovieById(id);
    }
}