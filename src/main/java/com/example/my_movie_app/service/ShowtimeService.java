package com.example.my_movie_app.service;


import com.example.my_movie_app.dto.CinemaShowtimeDto;
import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.dto.ShowtimeDto;
import com.example.my_movie_app.dto.request.ShowtimeRequest;
import com.example.my_movie_app.entity.*;
import com.example.my_movie_app.repository.MovieRepository;
import com.example.my_movie_app.repository.RoomRepository;
import com.example.my_movie_app.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;
    private static final Logger log = LoggerFactory.getLogger(ShowtimeService.class);

    public double calculateDistance(
            double lat1, double lon1,
            double lat2, double lon2) {

        final int R = 6371;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c;

        return Math.round(distance * 10.0) / 10.0;
    }

    public Showtime createShowtime(ShowtimeRequest request) {

        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Room not found"));

        LocalDateTime endTime = request.getStartTime()
                .plusMinutes(movie.getDurationMinutes())
                .plusMinutes(15);

        boolean isConflict = showtimeRepository
                .existsByRoomAndStatusAndStartTimeLessThanAndEndTimeGreaterThan(
                        room,
                        "ACTIVE",
                        endTime,
                        request.getStartTime()
                );

        if (isConflict) {
            throw new RuntimeException("Phòng đã có suất chiếu trong khoảng thời gian này!");
        }

        boolean isWeekend = isWeekend(request.getStartTime().getDayOfWeek());

        BigDecimal weekendModifier = isWeekend
                ? new BigDecimal("10000")
                : BigDecimal.ZERO;

        BigDecimal finalPrice = request.getBasePrice().add(weekendModifier);

        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(request.getStartTime())
                .endTime(endTime)
                .basePrice(finalPrice)
                .weekendModifier(weekendModifier)
                .status("ACTIVE")
                .build();

        return showtimeRepository.save(showtime);
    }

    // ================= HELPER =================
    private boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }


    public List<LocalDate> getNext10ShowDates(UUID movieId) {

        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh"));

        List<java.sql.Date> raw = showtimeRepository.findNext10ShowDates(movieId, now);

        List<LocalDate> result = raw.stream()
                .map(java.sql.Date::toLocalDate)
                .toList();
        return result;
    }

    public List<CinemaShowtimeDto> getCinemaShowtimes(
            UUID movieId,
            LocalDate date,
            double userLat,
            double userLng
    ) {

        ZoneId zone = ZoneId.of("Asia/Ho_Chi_Minh");

        LocalDate today = LocalDate.now(zone);
        LocalDateTime now = LocalDateTime.now(zone);

        LocalDateTime start;
        LocalDateTime end;

        if (date.equals(today)) {
            start = now;
            end = date.atTime(23, 59, 59);
        } else {
            start = date.atStartOfDay();
            end = date.atTime(23, 59, 59);
        }

        List<Showtime> showtimes = showtimeRepository
                .findShowtimesByMovieAndTime(movieId, start, end);

        // Group theo cinema
        Map<Cinema, List<Showtime>> map = new HashMap<>();

        for (Showtime s : showtimes) {
            Cinema cinema = s.getRoom().getCinema();
            map.computeIfAbsent(cinema, k -> new ArrayList<>()).add(s);
        }

        // Build DTO
        List<CinemaShowtimeDto> result = new ArrayList<>();

        for (Map.Entry<Cinema, List<Showtime>> entry : map.entrySet()) {

            Cinema cinema = entry.getKey();
            List<Showtime> sts = entry.getValue();

            double distance = calculateDistance(
                    userLat, userLng,
                    cinema.getLatitude(), cinema.getLongitude()
            );

            List<ShowtimeDto> showtimeDtos = sts.stream()
                    .sorted(Comparator.comparing(Showtime::getStartTime))
                    .map(s -> new ShowtimeDto(
                            s.getId().toString(),
                            s.getStartTime().toString()
                    ))
                    .toList();

            result.add(new CinemaShowtimeDto(
                    cinema.getId().toString(),
                    cinema.getName(),
                    distance,
                    showtimeDtos
            ));
        }

        // sort theo distance
        result.sort(Comparator.comparing(CinemaShowtimeDto::getDistanceKm));

        return result;
    }

    public MovieDto getMovieByShowtime(UUID showtimeId) {
        Showtime showtime = showtimeRepository.findFullById(showtimeId)
                .orElseThrow(() -> new RuntimeException("Showtime not found"));

        Movie movie = showtime.getMovie();

        return mapToDto(movie);
    }


    private MovieDto mapToDto(Movie movie) {
        return MovieDto.builder()
                .id(movie.getId())
                .title(movie.getTitle())
                .durationMinutes(movie.getDurationMinutes())
                .posterUrl(movie.getPosterUrl())
                .ageRating(movie.getAgeRating())
                .language(movie.getLanguage())
                .trailerUrl(movie.getTrailerUrl())
                .releaseDate(movie.getReleaseDate().toString())
                .description(movie.getDescription())
                .genres(
                        movie.getGenres()
                                .stream()
                                .map(Genre::getName)
                                .toList()
                )
                .build();
    }
}