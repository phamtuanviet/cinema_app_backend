package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.dto.PageResponse;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.service.MovieService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/movie")
@RequiredArgsConstructor
public class MovieController {

    private final MovieService movieService;

    @PostMapping
    public Movie create(@RequestBody Movie movie) {
        return movieService.create(movie);
    }

    @GetMapping("/coming-soon")
    public PageResponse<MovieDto> getMoviesComingSoon(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return movieService.getMoviesComingSoon(search, page, size);
    }

    @GetMapping("/now-showing")
    public PageResponse<MovieDto> getMoviesNowShowing(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return movieService.getMoviesNowShowing(search, page, size);
    }

    @PutMapping("/{id}")
    public Movie update(@PathVariable UUID id, @RequestBody Movie movie) {
        return movieService.update(id, movie);
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable UUID id) {
        movieService.delete(id);
        return "Deleted successfully";
    }

    @GetMapping("/{id}")
    public MovieDto getById(@PathVariable UUID id) {
        return movieService.getMovieById(id);
    }
}