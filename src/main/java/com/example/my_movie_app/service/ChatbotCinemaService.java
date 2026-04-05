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

    public List<CinemaNearbyResponse> getNearbyCinemas(Double lat, Double lng, Double radius) {
        List<CinemaNearbyResponse> cinemas = cinemaRepository.findCinemasNearby(lat, lng, radius != null ? radius : 10.0);

        if (cinemas.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Không có rạp nào trong bán kính " + radius + "km quanh bạn.");
        }
        return cinemas;
    }
}
