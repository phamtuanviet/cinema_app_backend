package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.dto.PageResponse;
import com.example.my_movie_app.dto.request.MovieRequest;
import com.example.my_movie_app.entity.Genre;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.entity.Rating;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.repository.GenreRepository;
import com.example.my_movie_app.repository.MovieRepository;
import com.example.my_movie_app.repository.ShowtimeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

        // 🔥 1. Tính toán Rating Trung Bình
        double averageRating = 0.0;
        if (m.getRatings() != null && !m.getRatings().isEmpty()) {
            averageRating = m.getRatings().stream()
                    .mapToDouble(Rating::getScore)
                    .average()
                    .orElse(0.0);

            // Làm tròn 1 chữ số thập phân (Ví dụ: 4.56 -> 4.6)
            averageRating = Math.round(averageRating * 10.0) / 10.0;
        }

        if(averageRating == 0) {
            averageRating = 5.0;
        }

        // 🔥 2. Map sang DTO (Đã fix lỗi dư dấu phẩy ở m.getGenres())
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
                        .toList(),
                averageRating // Truyền số sao trung bình vào biến cuối cùng
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<MovieDto> getMoviesComingSoon(String search, int page, int size) {
        System.out.println(search);
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDate today = LocalDate.now(zone);
        Pageable pageable = PageRequest.of(page, size);

        // 🔥 Biến null thành chuỗi rỗng
        String safeSearch = (search == null) ? "" : search;

        Page<Movie> moviePage = showtimeRepository.findComingSoon(today, safeSearch, pageable);
        return mapToPageResponse(moviePage);
    }

    @Transactional(readOnly = true)
    public PageResponse<MovieDto> getMoviesNowShowing(String search, int page, int size) {
        System.out.println(search);
        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");
        LocalDateTime now = LocalDateTime.now(zone);
        LocalDate today = now.toLocalDate();
        Pageable pageable = PageRequest.of(page, size);

        // 🔥 Biến null thành chuỗi rỗng
        String safeSearch = (search == null) ? "" : search;

        Page<Movie> moviePage = showtimeRepository.findNowShowing(now, today, safeSearch, pageable);
        return mapToPageResponse(moviePage);
    }

    private PageResponse<MovieDto> mapToPageResponse(Page<Movie> moviePage) {
        // Nếu trang trống thì trả về luôn, khỏi tốn công gọi DB
        if (moviePage.isEmpty()) {
            return new PageResponse<>(List.of(), moviePage.isLast(), moviePage.getTotalPages(), moviePage.getTotalElements());
        }

        // 1. Trích xuất danh sách ID của các phim nằm trong trang hiện tại (VD: lấy 10 ID)
        List<UUID> movieIds = moviePage.getContent().stream()
                .map(Movie::getId)
                .toList();

        // 2. CHỦ ĐỘNG lấy các phim đó kèm theo Thể loại (Genres) từ DB
        // Lúc này Hibernate sẽ query và nạp sẵn genres, không còn bị Lazy nữa
        List<Movie> moviesWithGenres = movieRepository.findAllMoviesWithDetailsByIdIn(movieIds);

        // Bỏ vào Map để tra cứu lại nhanh chóng theo ID
        Map<UUID, Movie> movieMap = moviesWithGenres.stream()
                .collect(Collectors.toMap(Movie::getId, m -> m));

        // 3. Bắt đầu Map sang DTO
        List<MovieDto> dtos = moviePage.getContent().stream()
                .map(m -> {
                    // Lấy ra bộ phim ĐÃ ĐƯỢC NẠP SẴN GENRES từ Map
                    Movie fullMovie = movieMap.get(m.getId());
                    double averageRating = 0.0;
                    if (fullMovie.getRatings() != null && !fullMovie.getRatings().isEmpty()) {
                        averageRating = fullMovie.getRatings().stream()
                                .mapToDouble(Rating::getScore)
                                .average()
                                .orElse(0.0);
                        // Làm tròn 1 chữ số thập phân
                        averageRating = Math.round(averageRating * 10.0) / 10.0;
                    }

                    // Điểm mặc định nếu chưa ai đánh giá
                    if (averageRating == 0) {
                        averageRating = 5.0;
                    }
                    return new MovieDto(
                            fullMovie.getId(),
                            fullMovie.getTitle(),
                            fullMovie.getDurationMinutes(),
                            fullMovie.getPosterUrl(),
                            fullMovie.getAgeRating(),
                            fullMovie.getLanguage(),
                            fullMovie.getTrailerUrl(),
                            fullMovie.getReleaseDate() != null ? fullMovie.getReleaseDate().toString() : null,
                            fullMovie.getDescription(),

                            // Lúc này gọi getGenres() cực kỳ an toàn, vì nó đã có sẵn data!
                            fullMovie.getGenres().stream()
                                    .map(Genre::getName)
                                    .toList(),

                            averageRating
                    );
                })
                .toList();

        return new PageResponse<>(
                dtos,
                moviePage.isLast(),
                moviePage.getTotalPages(),
                moviePage.getTotalElements()
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

}