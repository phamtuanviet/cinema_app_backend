package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.dto.request.MovieRequest;
import com.example.my_movie_app.entity.Genre;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.repository.GenreRepository;
import com.example.my_movie_app.repository.MovieRepository;
import com.example.my_movie_app.repository.ShowtimeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;


import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final ShowtimeRepository showtimeRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void importMoviesFromJson(InputStream inputStream) {
        try {
            List<MovieRequest> requests = objectMapper.readValue(
                    inputStream,
                    new TypeReference<List<MovieRequest>>() {}
            );

            // 🔥 1. Lấy toàn bộ genre name từ JSON
            Set<String> genreNames = requests.stream()
                    .flatMap(m -> m.getGenres().stream())
                    .map(g -> normalize(g.getName()))
                    .collect(Collectors.toSet());

            // 🔥 2. Query 1 lần tất cả genre trong DB
            List<Genre> existingGenres = genreRepository.findAll();

            Map<String, Genre> genreMap = new HashMap<>();
            for (Genre g : existingGenres) {
                genreMap.put(normalize(g.getName()), g);
            }

            // 🔥 3. Tạo genre chưa có (batch)
            List<Genre> newGenres = new ArrayList<>();

            for (String name : genreNames) {
                if (!genreMap.containsKey(name)) {
                    Genre g = Genre.builder()
                            .name(capitalize(name))
                            .build();
                    newGenres.add(g);
                    genreMap.put(name, g);
                }
            }

            if (!newGenres.isEmpty()) {
                genreRepository.saveAll(newGenres);
            }

            // 🔥 4. Build movie list (không save từng cái)
            List<Movie> movies = new ArrayList<>();

            for (MovieRequest req : requests) {

                List<Genre> genres = req.getGenres().stream()
                        .map(g -> genreMap.get(normalize(g.getName())))
                        .collect(Collectors.toList());

                if (movieRepository.existsByTitleAndReleaseDate(req.getTitle(), req.getReleaseDate())) {
                    continue;
                }

                Movie movie = Movie.builder()
                        .title(req.getTitle().trim())
                        .description(req.getDescription())
                        .durationMinutes(req.getDurationMinutes())
                        .releaseDate(req.getReleaseDate())
                        .posterUrl(req.getPosterUrl())
                        .trailerUrl(req.getTrailerUrl())
                        .ageRating(req.getAgeRating())
                        .language(req.getLanguage())
                        .isActive(req.getIsActive())
                        .genres(genres)
                        .build();

                movies.add(movie);
            }

            // 🔥 5. Save ALL (batch insert)
            movieRepository.saveAll(movies);

        } catch (Exception e) {
            throw new RuntimeException("Import failed", e);
        }
    }

    private String normalize(String name) {
        return name.trim().toLowerCase();
    }

    private String capitalize(String name) {
        return Arrays.stream(name.split(" "))
                .map(w -> w.substring(0, 1).toUpperCase() + w.substring(1))
                .collect(Collectors.joining(" "));
    }



    public Movie create(Movie movie) {

        List<Genre> processedGenres = movie.getGenres().stream().map(g -> {
            return genreRepository.findByNameIgnoreCase(g.getName())
                    .orElseGet(() -> genreRepository.save(
                            Genre.builder().name(g.getName()).build()
                    ));
        }).toList();

        movie.setGenres(processedGenres);

        return movieRepository.save(movie);
    }

    public Movie update(UUID id, Movie movie) {
        Movie existing = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        existing.setTitle(movie.getTitle());
        existing.setDescription(movie.getDescription());
        existing.setDurationMinutes(movie.getDurationMinutes());
        existing.setReleaseDate(movie.getReleaseDate());
        existing.setBasePrice(movie.getBasePrice());
        existing.setPosterUrl(movie.getPosterUrl());
        existing.setTrailerUrl(movie.getTrailerUrl());
        existing.setAgeRating(movie.getAgeRating());
        existing.setLanguage(movie.getLanguage());
        existing.setIsActive(movie.getIsActive());
        existing.setGenres(movie.getGenres());

        return movieRepository.save(existing);
    }

    public void delete(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        movie.setIsActive(false);
        movieRepository.save(movie);
    }

    public MovieDto getMovieById(UUID id) {
        Movie m = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        System.out.println(
                "Domixi " + m.getGenres()
                        .stream()
                        .map(Genre::getName)
                        .toList()
        );
        return new MovieDto(
                m.getId(),
                m.getTitle(),
                m.getDurationMinutes(),
                m.getPosterUrl(),
                m.getAgeRating(),
                m.getLanguage(),
                m.getTrailerUrl(),
                m.getReleaseDate().toString(),
                m.getDescription(),
                m.getGenres().stream()
                        .map(Genre::getName)
                        .toList()
        );
    }

    public List<Movie> getAll() {
        return movieRepository.findByIsActiveTrue();
    }

    public String getMovieStatus(Movie movie) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        if (movie.getReleaseDate().isAfter(today)) {
            return "COMING_SOON";
        }

        boolean hasFutureShowtime = showtimeRepository.existsByMovieIdAndStartTimeAfter(
                movie.getId(),
                LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"))
        );

        if (hasFutureShowtime) {
            return "NOW_SHOWING";
        }

        return "ENDED";
    }


    public List<MovieDto> getMoviesNowShowing() {

        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

        LocalDateTime now = LocalDateTime.now(zone);
        LocalDate today = LocalDate.now(zone);

        List<Movie> movies = showtimeRepository.findNowShowing(now, today);

        if (movies.isEmpty()) return List.of();

        return movies.stream()
                .map(m -> new MovieDto(
                        m.getId(),
                        m.getTitle(),
                        m.getDurationMinutes(),
                        m.getPosterUrl(),
                        m.getAgeRating(),
                        m.getLanguage(),
                        m.getTrailerUrl(),
                        m.getReleaseDate().toString(),
                        m.getDescription(),
                        m.getGenres().stream()
                                .map(Genre::getName)
                                .toList()
                ))
                .toList();
    }
    public List<MovieDto> getMoviesComingSoon() {
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

        LocalDate today = LocalDate.now(zone);

        List<Movie> movies = showtimeRepository.findComingSoon(today);

        if (movies.isEmpty()) return List.of();

        return movies.stream()
                .map(m -> new MovieDto(
                        m.getId(),
                        m.getTitle(),
                        m.getDurationMinutes(),
                        m.getPosterUrl(),
                        m.getAgeRating(),
                        m.getLanguage(),
                        m.getTrailerUrl(),
                        m.getReleaseDate().toString(),
                        m.getDescription(),
                        m.getGenres().stream()
                                .map(Genre::getName)
                                .toList()
                ))
                .toList();
    }

}