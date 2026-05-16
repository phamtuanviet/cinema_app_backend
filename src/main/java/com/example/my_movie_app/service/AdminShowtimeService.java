package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.AdminPaginatedResponse;
import com.example.my_movie_app.dto.AdminShowtimeDto;
import com.example.my_movie_app.dto.AdminShowtimeUpdateRequest;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.repository.ShowtimeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminShowtimeService {

    private final ShowtimeRepository showtimeRepository;

    public AdminPaginatedResponse<AdminShowtimeDto> getShowtimes(String search, String filter, int page, int size) {

        // 🔥 Xử lý chuỗi tìm kiếm ngay trên Java
        String searchParam = null;
        if (search != null && !search.trim().isEmpty()) {
            searchParam = "%" + search.trim().toLowerCase() + "%";
        }

        LocalDateTime now = LocalDateTime.now();
        Page<Showtime> showtimePage;

        switch (filter.toUpperCase()) {
            case "ONGOING":
                Pageable ongoingPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "endTime"));
                showtimePage = showtimeRepository.findOngoing(searchParam, now, ongoingPageable);
                break;

            case "PAST":
                Pageable pastPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "endTime"));
                showtimePage = showtimeRepository.findPast(searchParam, now, pastPageable);
                break;

            case "UPCOMING":
            default:
                Pageable upcomingPageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "startTime"));
                showtimePage = showtimeRepository.findUpcoming(searchParam, now, upcomingPageable);
                break;
        }

        List<AdminShowtimeDto> dtoList = showtimePage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return new AdminPaginatedResponse<>(
                dtoList,
                showtimePage.getNumber(),
                showtimePage.getTotalPages(),
                showtimePage.getTotalElements(),
                showtimePage.isLast()
        );
    }
    // Hàm ánh xạ
    public AdminShowtimeDto mapToDto(Showtime showtime) {
        // Kiểm tra an toàn để tránh NullPointerException nếu các liên kết bị lỗi trong DB
        String movieName = showtime.getMovie() != null ? showtime.getMovie().getTitle() : "N/A";
        String posterUrl = showtime.getMovie() != null ? showtime.getMovie().getPosterUrl() : null;
        String roomName = showtime.getRoom() != null ? showtime.getRoom().getName() : "N/A";
        String cinemaName = (showtime.getRoom() != null && showtime.getRoom().getCinema() != null)
                ? showtime.getRoom().getCinema().getName() : "N/A";

        return AdminShowtimeDto.builder()
                .id(showtime.getId())
                .movieName(movieName)
                .moviePosterUrl(posterUrl)
                .cinemaName(cinemaName)
                .roomName(roomName)
                .startTime(showtime.getStartTime())
                .endTime(showtime.getEndTime())
                .basePrice(showtime.getBasePrice())
                .status(showtime.getStatus())
                .build();
    }

    public AdminShowtimeDto getShowtimeById(UUID id) {
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lịch chiếu với ID: " + id));
        return mapToDto(showtime);
    }

    // 2. API Cập nhật Lịch chiếu
    @Transactional
    public AdminShowtimeDto updateShowtime(UUID id, AdminShowtimeUpdateRequest request) {
        // Tìm lịch chiếu hiện tại
        Showtime showtime = showtimeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Lịch chiếu"));

        // Kiểm tra logic thời gian cơ bản
        if (request.getStartTime().isAfter(request.getEndTime())) {
            throw new RuntimeException("Giờ bắt đầu không thể diễn ra sau giờ kết thúc!");
        }

        // Logic check trùng phòng:
        // Nếu Admin CANCELED lịch chiếu -> Không cần check trùng
        // Nếu Admin đổi giờ và giữ ACTIVE -> Phải check trùng với các lịch khác
        if ("ACTIVE".equalsIgnoreCase(request.getStatus())) {
            boolean isConflict = showtimeRepository.existsConflictForUpdate(
                    showtime.getRoom().getId(),
                    id,
                    request.getStartTime(),
                    request.getEndTime()
            );
            if (isConflict) {
                throw new RuntimeException("Giờ chiếu mới bị trùng với một lịch chiếu khác đang hoạt động trong cùng phòng!");
            }
        }

        // Cập nhật dữ liệu
        showtime.setStartTime(request.getStartTime());
        showtime.setEndTime(request.getEndTime());
        showtime.setStatus(request.getStatus().toUpperCase());

        // Lưu và trả về
        Showtime savedShowtime = showtimeRepository.save(showtime);
        return mapToDto(savedShowtime);
    }
}