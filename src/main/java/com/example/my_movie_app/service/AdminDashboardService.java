package com.example.my_movie_app.service;


import com.example.my_movie_app.dto.DashboardStatsDto;
import com.example.my_movie_app.dto.RevenuePointDto;
import com.example.my_movie_app.projection.RevenueProjection;
import com.example.my_movie_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final ShowtimeRepository showtimeRepository;
    private final BookingRepository bookingRepository;

    public DashboardStatsDto getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalMovies = movieRepository.count();
        long totalCinemas = cinemaRepository.count();
        long totalShowtimes = showtimeRepository.count();

        return new DashboardStatsDto(totalUsers, totalMovies, totalCinemas, totalShowtimes);
    }

    public List<RevenuePointDto> getRevenueLast7Days() {
        List<RevenueProjection> projections = bookingRepository.getRevenueLast7Days();

        return projections.stream()
                .map(p -> new RevenuePointDto(p.getDate(), p.getAmount() != null ? p.getAmount() : 0.0))
                .collect(Collectors.toList());
    }
}