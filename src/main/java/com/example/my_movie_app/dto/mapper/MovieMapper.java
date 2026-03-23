package com.example.my_movie_app.dto.mapper;

import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.entity.Genre;
import com.example.my_movie_app.entity.Movie;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class MovieMapper {

    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("dd-MM-yyyy");

    public static MovieDto toDto(Movie movie) {
        if (movie == null) return null;

        return MovieDto.builder()
                .id(movie.getId()) // UUID giữ nguyên
                .title(movie.getTitle())
                .durationMinutes(movie.getDurationMinutes())
                .posterUrl(movie.getPosterUrl())
                .ageRating(movie.getAgeRating())
                .language(movie.getLanguage())
                .trailerUrl(movie.getTrailerUrl())
                .releaseDate(
                        movie.getReleaseDate() != null
                                ? movie.getReleaseDate().format(DATE_FORMAT)
                                : null
                )
                .description(movie.getDescription())
                .genres(mapGenres(movie.getGenres()))
                .build();
    }

    private static List<String> mapGenres(List<Genre> genres) {
        if (genres == null) return List.of();

        return genres.stream()
                .map(Genre::getName) // giả định Genre có field "name"
                .collect(Collectors.toList());
    }
}