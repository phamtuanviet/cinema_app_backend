package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.MovieDto;
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
    public List<MovieDto> getMoviesComingSoon() {
        return movieService.getMoviesComingSoon();
    }

    @GetMapping("/now-showing")
    public List<MovieDto> getMoviesNowShowing() {
        return movieService.getMoviesNowShowing();
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