package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.response.CinemaNearbyResponse;
import com.example.my_movie_app.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatbotCinemaService {
    private final CinemaRepository cinemaRepository;

    public List<CinemaNearbyResponse> getNearbyCinemas(Double lat, Double lng, Double radius, String movieTitle, String query) {
        // Tự đánh giá xem khách có gửi tọa độ không
        boolean hasLocation = (lat != null && lng != null);

        // Nếu không có tọa độ, truyền 0.0 xuống để PostgreSQL không bị lỗi bytea
        Double safeLat = hasLocation ? lat : 0.0;
        Double safeLng = hasLocation ? lng : 0.0;

        // Truyền thêm cờ hasLocation xuống Repository
        List<CinemaNearbyResponse> cinemas = cinemaRepository.findCinemasNearby(
                safeLat,
                safeLng,
                radius != null ? radius : 10.0,
                movieTitle,
                query,
                hasLocation
        );

        if (cinemas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không tìm thấy rạp nào phù hợp với yêu cầu của bạn.");
        }
        return cinemas;
    }
}
