package com.example.my_movie_app.controller;

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
    public Movie getById(@PathVariable UUID id) {
        return movieService.getById(id);
    }

    @GetMapping
    public List<Movie> getAll() {
        return movieService.getAll();
    }

    @GetMapping("/search")
    public List<Movie> search(@RequestParam String keyword) {
        return movieService.search(keyword);
    }
}