package com.example.my_movie_app.service;


import com.example.my_movie_app.dto.request.MovieSearchRequest;
import com.example.my_movie_app.dto.request.NowPlayingRequest;
import com.example.my_movie_app.dto.response.MovieChatbotResponse;
import com.example.my_movie_app.dto.response.MovieDetailResponse;
import com.example.my_movie_app.entity.Genre;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.entity.Rating;
import com.example.my_movie_app.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ChatbotMovieService {

    private final MovieRepository movieRepository;

    private MovieDetailResponse mapToDetailResponse(Movie movie) {
        // 1. Tính điểm đánh giá trung bình và tổng số lượt đánh giá
        double avgRating = 0.0;
        int totalRatings = 0; // Khởi tạo biến đếm

        if (movie.getRatings() != null && !movie.getRatings().isEmpty()) {
            totalRatings = movie.getRatings().size(); // Lấy số lượng người đánh giá

            avgRating = movie.getRatings().stream()
                    .mapToInt(Rating::getScore)
                    .average()
                    .orElse(0.0);
        }

        // Làm tròn rating đến 1 chữ số thập phân
        double roundedRating = Math.round(avgRating * 10.0) / 10.0;

        // 2. Chuyển đổi danh sách Object Genre thành danh sách tên thể loại (String)
        List<String> genreNames = movie.getGenres().stream()
                .map(Genre::getName)
                .toList();

        // 3. Build object trả về
        return MovieDetailResponse.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .genres(genreNames)
                .durationMinutes(movie.getDurationMinutes())
                .ageRating(movie.getAgeRating())
                .language(movie.getLanguage())
                .releaseDate(movie.getReleaseDate())
                .averageRating(roundedRating)
                .ratingCount(totalRatings) // Set số lượt đánh giá vào DTO
                .build();
    }

    @Transactional(readOnly = true)
    public Page<MovieChatbotResponse> searchMoviesForChatbot(MovieSearchRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        LocalDateTime now = LocalDateTime.now();

        // CHỮA LỖI POSTGRESQL: Chuyển null thành chuỗi rỗng ""
        // để các hàm LOWER() và CONCAT() trong JPQL không bị lỗi bytea
        String keyword = (request.getKeyword() == null) ? "" : request.getKeyword().trim();
        String genre = (request.getGenre() == null) ? "" : request.getGenre().trim();

        Page<Movie> moviePage = movieRepository.searchMoviesWithActiveShowtimes(
                keyword,
                genre,
                request.getLanguage(), // Giữ nguyên, truyền null được
                request.getAgeRating(), // Giữ nguyên, truyền null được
                now,
                pageable
        );

        return moviePage.map(this::mapToResponse);
    }

    private MovieChatbotResponse mapToResponse(Movie movie) {
        // Tính điểm đánh giá trung bình
        double avgRating = 0.0;
        if (movie.getRatings() != null && !movie.getRatings().isEmpty()) {
            avgRating = movie.getRatings().stream()
                    .mapToInt(Rating::getScore)
                    .average()
                    .orElse(0.0);
        }

        // Map danh sách thể loại thành String
        List<String> genreNames = movie.getGenres().stream()
                .map(Genre::getName)
                .collect(Collectors.toList());

        // Làm tròn rating đến 1 chữ số thập phân
        double roundedRating = Math.round(avgRating * 10.0) / 10.0;

        return MovieChatbotResponse.builder()
                .movieId(movie.getId())
                .title(movie.getTitle())
                .posterUrl(movie.getPosterUrl())
                .genres(genreNames)
                .durationMinutes(movie.getDurationMinutes())
                .ageRating(movie.getAgeRating())
                .averageRating(roundedRating)
                .build();
    }

    @Transactional(readOnly = true)
    public Page<MovieChatbotResponse> getNowPlayingMovies(NowPlayingRequest request) {
        LocalDateTime fromDate;
        LocalDateTime toDate = null;
        LocalDateTime now = LocalDateTime.now();

        // CHỮA LỖI: Ép locationQuery thành chuỗi rỗng để tránh lỗi lower(bytea)
        String loc = (request.getLocationQuery() == null) ? "" : request.getLocationQuery().trim();

        if (request.getDate() != null) {
            LocalDate requestedDate = request.getDate();
            if (requestedDate.isEqual(now.toLocalDate())) {
                fromDate = now;
                toDate = requestedDate.atTime(LocalTime.MAX);
            } else if (requestedDate.isBefore(now.toLocalDate())) {
                fromDate = now;
            } else {
                fromDate = requestedDate.atStartOfDay();
                toDate = requestedDate.atTime(LocalTime.MAX);
            }
        } else {
            fromDate = now;
        }

        // Truyền biến 'loc' đã xử lý rỗng vào đây
        Page<Movie> moviePage = movieRepository.getNowPlayingMovies(
                loc,
                fromDate,
                toDate,
                PageRequest.of(request.getPage(), request.getSize())
        );

        return moviePage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<MovieChatbotResponse> getComingSoonMovies(int page, int size) {
        LocalDate today = LocalDate.now();
        Pageable pageable = PageRequest.of(page, size);

        Page<Movie> moviePage = movieRepository.getComingSoonMovies(today, pageable);

        return moviePage.map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public MovieDetailResponse getMovieDetailByTitle(String title, Integer year, String language) {

        // Xử lý null thành chuỗi rỗng để tránh lỗi lower(bytea) của PostgreSQL
        String searchTitle = (title == null) ? "" : title.trim();
        String searchLanguage = (language == null) ? "" : language.trim();

        // Lấy danh sách phim khớp điều kiện, ưu tiên phim mới nhất (do đã ORDER BY ở query)
        List<Movie> movies = movieRepository.findMovieDetailForChatbot(searchTitle, year, searchLanguage);

        if (movies.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Xin lỗi, tôi không tìm thấy thông tin chi tiết cho phim: " + (title != null ? title : "này")
            );
        }

        // Luôn lấy bộ phim đầu tiên (trùng khớp nhất và mới nhất)
        Movie selectedMovie = movies.get(0);

        return mapToDetailResponse(selectedMovie); // Dùng lại hàm map đã viết trước đó
    }

}