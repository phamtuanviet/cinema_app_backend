package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.response.BookingHistoryResponse;
import com.example.my_movie_app.dto.response.CinemaNearbyResponse;
import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.repository.BookingRepository;
import com.example.my_movie_app.repository.CinemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotBookingService {

    private final BookingRepository bookingRepository;

    @Transactional(readOnly = true)
    public List<BookingHistoryResponse> getMyHistory(UUID userId, int limit) {
        // Lấy danh sách booking mới nhất
        Pageable pageable = PageRequest.of(0, limit, Sort.by("createdAt").descending());
        List<Booking> bookings = bookingRepository.findByUserId(userId, pageable);

        return bookings.stream().map(this::mapToResponse).toList();
    }

    private BookingHistoryResponse mapToResponse(Booking b) {
        String displayStatus = "Đang xử lý";
        Instant now = Instant.now();

        // 1. Giải mã trạng thái thực tế từ SeatHoldSession
        if (b.getStatus() == BookingStatus.PAID) {
            displayStatus = "Thành công (Đã thanh toán)";
        } else if (b.getStatus() == BookingStatus.CANCELLED) {
            displayStatus = "Đã hủy";
        } else if (b.getStatus() == BookingStatus.PENDING) {
            // Kiểm tra session hết hạn chưa
            if (b.getSession() != null && b.getSession().getExpiresAt().isBefore(now)) {
                displayStatus = "Đã hết hạn (Quá 15p giữ chỗ)";
            } else {
                displayStatus = "Chờ thanh toán VNPAY";
            }
        }


        String seatNames = "";
        if (b.getSession() != null && b.getSession().getSeatReservations() != null) {
            seatNames = b.getSession().getSeatReservations().stream()
                    .filter(sr -> !sr.isCancel()) // Chỉ lấy ghế chưa bị hủy
                    .map(sr -> sr.getSeat().getSeatRow() + sr.getSeat().getSeatNumber())
                    .collect(Collectors.joining(", "));
        }

        return BookingHistoryResponse.builder()
                .ticketCode(b.getTicketCode())
                .movieTitle(b.getShowtime().getMovie().getTitle())
                .cinemaName(b.getShowtime().getRoom().getCinema().getName())
                .roomName(b.getShowtime().getRoom().getName())
                .startTime(b.getShowtime().getStartTime())
                .seats(seatNames)
                .totalAmount(b.getTotalAmount())
                .statusDisplay(displayStatus)
                .qrCodeUrl(b.getStatus() == BookingStatus.PAID ? b.getQrCodeUrl() : null)
                .build();
    }
}