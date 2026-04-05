package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.request.MovieSearchRequest;
import com.example.my_movie_app.dto.request.NowPlayingRequest;
import com.example.my_movie_app.dto.response.*;
import com.example.my_movie_app.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotMovieService chatbotMovieService;
    private final ChatbotShowtimeService chatbotShowtimeService;
    private final ChatbotCinemaService chatbotCinemaService;
    private final ChatbotUserService chatbotUserService;
    private final ChatbotVoucherService chatbotVoucherService;
    private final ChatbotBookingService bookingService;

    @GetMapping("/me/bookings")
    public ResponseEntity<List<BookingHistoryResponse>> getMyBookings(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(defaultValue = "5") int limit) {

        return ResponseEntity.ok(bookingService.getMyHistory(userId, limit));
    }

    @GetMapping("/me/vouchers")
    public ResponseEntity<List<UserVoucherChatbotResponse>> getMyVouchers(
            @RequestHeader("X-User-Id") UUID userId,
            @RequestParam(required = false, defaultValue = "active") String status) {

        List<UserVoucherChatbotResponse> vouchers = chatbotVoucherService.getMyVouchers(userId, status);

        return ResponseEntity.ok(vouchers);
    }

    @GetMapping("/me/points")
    public ResponseEntity<UserPointsResponse> getMyPoints(@RequestHeader("X-User-Id") UUID userId) {
        return ResponseEntity.ok(chatbotUserService.getMyPoints(userId));
    }

    @GetMapping("/movies/search")
    public ResponseEntity<Page<MovieChatbotResponse>> searchMovies(
            @ModelAttribute MovieSearchRequest request) {

        Page<MovieChatbotResponse> result = chatbotMovieService.searchMoviesForChatbot(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/movies/now-playing")
    public ResponseEntity<Page<MovieChatbotResponse>> getNowPlaying(
            @ModelAttribute NowPlayingRequest request) {

        Page<MovieChatbotResponse> result = chatbotMovieService.getNowPlayingMovies(request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/movies/coming-soon")
    public ResponseEntity<Page<MovieChatbotResponse>> getComingSoon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MovieChatbotResponse> result = chatbotMovieService.getComingSoonMovies(page, size);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/showtimes")
    public ResponseEntity<List<ShowtimeChatbotResponse>> getShowtimes(
            @RequestParam(required = false) String movieTitle,
            @RequestParam(required = false) String date, // Đổi từ LocalDate sang String
            @RequestParam(required = false) String locationQuery) {

        // Chuyền thẳng chuỗi String xuống Service xử lý
        List<ShowtimeChatbotResponse> result = chatbotShowtimeService.getShowtimes(movieTitle, date, locationQuery);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/cinemas/nearby")
    public ResponseEntity<List<CinemaNearbyResponse>> getNearby(
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false) Double radius) {
        return ResponseEntity.ok(chatbotCinemaService.getNearbyCinemas(lat, lng, radius));
    }

    @GetMapping("/movies/detail")
    public ResponseEntity<MovieDetailResponse> getMovieDetail(
            @RequestParam String title,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String language) {

        MovieDetailResponse result = chatbotMovieService.getMovieDetailByTitle(title, year, language);
        return ResponseEntity.ok(result);
    }
}
