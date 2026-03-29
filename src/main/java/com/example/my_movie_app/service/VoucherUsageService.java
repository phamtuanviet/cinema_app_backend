package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.response.VoucherUsageResponse;
import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.repository.VoucherUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherUsageService {

    private final VoucherUsageRepository voucherUsageRepository;

    public List<VoucherUsageResponse> getUsageByUser(UUID userId) {

        return voucherUsageRepository.findByUserId(userId)
                .stream()
                .map(vu -> {

                    Booking booking = vu.getBooking();
                    Showtime showtime = booking.getShowtime();

                    return VoucherUsageResponse.builder()
                            .movieTitle(showtime.getMovie().getTitle())
                            .cinemaName(showtime.getRoom().getCinema().getName())
                            .roomName(showtime.getRoom().getName())
                            .showtime(showtime.getStartTime())
                            .discountAmount(vu.getDiscountAmount())
                            .usedAt(vu.getUsedAt())
                            .build();
                })
                .toList();
    }
}
