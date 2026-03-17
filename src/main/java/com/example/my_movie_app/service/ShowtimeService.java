package com.example.my_movie_app.service;


import com.example.my_movie_app.dto.request.ShowtimeRequest;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.entity.Room;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.repository.MovieRepository;
import com.example.my_movie_app.repository.RoomRepository;
import com.example.my_movie_app.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ShowtimeService {

    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final RoomRepository roomRepository;

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
}