package com.example.my_movie_app.controller;



import com.example.my_movie_app.dto.AdminGenreDto;
import com.example.my_movie_app.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin/genres")
@RequiredArgsConstructor
public class AdminGenreController {

    private final GenreRepository genreRepository;

    @GetMapping
    public List<AdminGenreDto> getAllGenres() {
        return genreRepository.findAll().stream()
                .map(g -> new AdminGenreDto(g.getId(), g.getName()))
                .collect(Collectors.toList());
    }
}