package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.*;
import com.example.my_movie_app.entity.Genre;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.repository.GenreRepository;
import com.example.my_movie_app.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminMovieService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final CloudinaryService cloudinaryService;

    public AdminPaginatedResponse<AdminMovieDto> getMovies(String search, int page, int size) {
        // Sắp xếp phim mới thêm lên đầu (Giả sử BaseEntity có trường createdAt)
        // Nếu không có, bạn có thể đổi thành Sort.by("releaseDate").descending()
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Movie> moviePage;

        // Xử lý tìm kiếm
        if (search != null && !search.trim().isEmpty()) {
            moviePage = movieRepository.findByTitleContainingIgnoreCase(search.trim(), pageable);
        } else {
            moviePage = movieRepository.findAll(pageable);
        }

        // Map list Entity -> list DTO
        List<AdminMovieDto> dtoList = moviePage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // Đóng gói vào PaginatedResponse
        return new AdminPaginatedResponse<>(
                dtoList,
                moviePage.getNumber(),
                moviePage.getTotalPages(),
                moviePage.getTotalElements(),
                moviePage.isLast()
        );
    }

    @Transactional
    public AdminMovieDto createMovie(AdminMovieCreateRequest request, MultipartFile poster) {

        // 1. Xử lý Upload Ảnh Poster
        String posterUrl = null;
        if (poster != null && !poster.isEmpty()) {
            posterUrl = cloudinaryService.uploadImage(poster);
        }

        // 2. Xử lý Thể loại (Genres)
        List<Genre> movieGenres = new ArrayList<>();

        // 2.1. Thêm các thể loại đã có sẵn dựa vào List ID
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            List<Genre> existingGenres = genreRepository.findAllById(request.getGenreIds());
            movieGenres.addAll(existingGenres);
        }

        // 2.2. Xử lý các thể loại người dùng tự gõ (newGenres)
        if (request.getNewGenres() != null && !request.getNewGenres().isEmpty()) {
            for (String newGenreName : request.getNewGenres()) {
                String trimmedName = newGenreName.trim();

                // Kiểm tra xem đã tồn tại trong DB chưa để tránh lỗi Unique Constraint
                Genre existingGenre = genreRepository.findByNameIgnoreCase(trimmedName).orElse(null);

                if (existingGenre != null) {
                    // Nếu DB đã có chữ này, chỉ cần add vào phim (tránh add trùng lặp)
                    if (!movieGenres.contains(existingGenre)) {
                        movieGenres.add(existingGenre);
                    }
                } else {
                    // Nếu chưa có, tạo mới Genre và lưu xuống DB
                    Genre newGenre = new Genre();
                    newGenre.setName(trimmedName);
                    newGenre = genreRepository.save(newGenre);
                    movieGenres.add(newGenre);
                }
            }
        }

        // 3. Khởi tạo đối tượng Movie
        Movie movie = Movie.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .durationMinutes(request.getDurationMinutes())
                .releaseDate(request.getReleaseDate())
                .basePrice(request.getBasePrice())
                .ageRating(request.getAgeRating())
                .language(request.getLanguage())
                .trailerUrl(request.getTrailerUrl())
                .isActive(request.getIsActive() != null ? request.getIsActive() : true)
                .posterUrl(posterUrl)
                .genres(movieGenres)
                .build();

        // 4. Lưu Phim
        Movie savedMovie = movieRepository.save(movie);

        // 5. Trả về DTO (Bạn tái sử dụng lại hàm mapToDto mình viết ở lần trước nhé)
        return mapToDto(savedMovie);
    }

    // Hàm chuyển đổi Entity -> DTO
    private AdminMovieDto mapToDto(Movie movie) {
        List<AdminGenreDto> genreDtos = movie.getGenres().stream()
                .map(g -> new AdminGenreDto(g.getId(), g.getName()))
                .collect(Collectors.toList());

        return AdminMovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .description(movie.getDescription())
                .durationMinutes(movie.getDurationMinutes())
                .releaseDate(movie.getReleaseDate())
                .basePrice(movie.getBasePrice())
                .posterUrl(movie.getPosterUrl())
                .trailerUrl(movie.getTrailerUrl())
                .ageRating(movie.getAgeRating())
                .language(movie.getLanguage())
                .isActive(movie.getIsActive())
                .genres(genreDtos)
                .build();
    }


    @Transactional
    public AdminMovieDto updateMovie(UUID id, AdminMovieUpdateRequest request, MultipartFile poster) {

        // 1. Tìm phim theo ID, nếu không thấy thì throw lỗi
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với ID: " + id));

        // 2. Cập nhật Poster NẾU client có gửi file ảnh mới lên
        if (poster != null && !poster.isEmpty()) {
            String newPosterUrl = cloudinaryService.uploadImage(poster);
            movie.setPosterUrl(newPosterUrl);
        }
        // Nếu poster == null, giữ nguyên url ảnh cũ trong DB

        // 3. Cập nhật các thông tin cơ bản
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setDurationMinutes(request.getDurationMinutes());
        movie.setReleaseDate(request.getReleaseDate());
        movie.setBasePrice(request.getBasePrice());
        movie.setAgeRating(request.getAgeRating());
        movie.setLanguage(request.getLanguage());
        movie.setTrailerUrl(request.getTrailerUrl());

        if (request.getIsActive() != null) {
            movie.setIsActive(request.getIsActive());
        }

        // 4. Xử lý cập nhật danh sách Thể loại (Genres)
        List<Genre> updatedGenres = new ArrayList<>();

        // 4.1. Add các thể loại cũ (dựa theo ID)
        if (request.getGenreIds() != null && !request.getGenreIds().isEmpty()) {
            updatedGenres.addAll(genreRepository.findAllById(request.getGenreIds()));
        }

        // 4.2. Xử lý các thể loại gõ mới
        if (request.getNewGenres() != null && !request.getNewGenres().isEmpty()) {
            for (String newGenreName : request.getNewGenres()) {
                String trimmedName = newGenreName.trim();

                Genre existingGenre = genreRepository.findByNameIgnoreCase(trimmedName).orElse(null);

                if (existingGenre != null) {
                    if (!updatedGenres.contains(existingGenre)) {
                        updatedGenres.add(existingGenre);
                    }
                } else {
                    Genre newGenre = new Genre();
                    newGenre.setName(trimmedName);
                    newGenre = genreRepository.save(newGenre);
                    updatedGenres.add(newGenre);
                }
            }
        }

        // Ghi đè danh sách thể loại mới cho phim
        movie.setGenres(updatedGenres);

        // 5. Lưu xuống DB và trả về DTO
        Movie savedMovie = movieRepository.save(movie);

        return mapToDto(savedMovie);
    }

    public AdminMovieDto getMovieById(UUID id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy phim với ID: " + id));

        return mapToDto(movie); // Sử dụng hàm mapping đã có
    }
}