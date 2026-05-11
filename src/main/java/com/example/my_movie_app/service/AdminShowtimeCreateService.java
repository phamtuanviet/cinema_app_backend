package com.example.my_movie_app.service;



import com.example.my_movie_app.dto.AdminShowtimeCreateRequest;
import com.example.my_movie_app.dto.AdminShowtimeDto;
import com.example.my_movie_app.dto.SimpleItemDto;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.entity.Room;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.repository.CinemaRepository;
import com.example.my_movie_app.repository.MovieRepository;
import com.example.my_movie_app.repository.RoomRepository;
import com.example.my_movie_app.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminShowtimeCreateService {

    private final MovieRepository movieRepository;
    private final CinemaRepository cinemaRepository;
    private final RoomRepository roomRepository;
    private final ShowtimeRepository showtimeRepository;

    // (Bạn có thể inject AdminShowtimeService cũ vào để dùng lại hàm mapToDto)
    private final AdminShowtimeService adminShowtimeService;

    // 1. Tìm Phim gợi ý
    public List<SimpleItemDto> searchMovies(String query) {
        return movieRepository.findTop10ByTitleContainingIgnoreCase(query)
                .stream()
                .map(m -> new SimpleItemDto(m.getId(), m.getTitle()))
                .collect(Collectors.toList());
    }

    // 2. Tìm Rạp gợi ý
    public List<SimpleItemDto> searchCinemas(String query) {
        return cinemaRepository.findTop10ByNameContainingIgnoreCase(query)
                .stream()
                .map(c -> new SimpleItemDto(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    // 3. Tìm Phòng trống
    public List<SimpleItemDto> getAvailableRooms(UUID cinemaId, LocalDateTime startTime, LocalDateTime endTime) {
        if (startTime.isAfter(endTime)) {
            throw new RuntimeException("Giờ bắt đầu không thể sau giờ kết thúc!");
        }

        return roomRepository.findAvailableRooms(cinemaId, startTime, endTime)
                .stream()
                .map(r -> new SimpleItemDto(r.getId(), r.getName()))
                .collect(Collectors.toList());
    }

    // 4. Tạo Lịch chiếu mới
    @Transactional
    public AdminShowtimeDto createShowtime(AdminShowtimeCreateRequest request) {
        // Lấy Movie & Room
        Movie movie = movieRepository.findById(request.getMovieId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Phim"));
        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Phòng chiếu"));

        // 🔥 DOUBLE CHECK: Đảm bảo phòng vẫn trống ngay tại mili-giây save data
        boolean isConflict = showtimeRepository.existsByRoomIdAndConflictTime(
                room.getId(), request.getStartTime(), request.getEndTime()
        );
        if (isConflict) {
            throw new RuntimeException("Phòng chiếu này vừa có lịch chiếu khác được đặt. Vui lòng chọn phòng khác!");
        }

        // Tạo Entity
        Showtime showtime = Showtime.builder()
                .movie(movie)
                .room(room)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .basePrice(request.getBasePrice())
                .weekendModifier(request.getWeekendModifier())
                // Gán Status tự động là Sắp chiếu, hoặc tuỳ logic của bạn
                .status("ACTIVE")
                .build();

        Showtime savedShowtime = showtimeRepository.save(showtime);

        // Gọi hàm map của service cũ để trả về DTO
        return adminShowtimeService.mapToDto(savedShowtime);
    }


}