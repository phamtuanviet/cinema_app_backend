package com.example.my_movie_app.dto.mapper;

import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.dto.ShowtimeDetailDto;
import com.example.my_movie_app.dto.response.BookingResponse;
import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.entity.Showtime;

public class BookingMapper {

    public static BookingResponse toResponse(Booking booking) {

        return BookingResponse.builder()
                .bookingId(booking.getId().toString())
                .ticketCode(booking.getTicketCode())
                .qrCodeUrl(booking.getQrCodeUrl())

                .seatAmount(booking.getSeatAmount())
                .comboAmount(booking.getComboAmount())
                .voucherDiscount(booking.getVoucherDiscount())
                .pointDiscount(booking.getPointDiscount())
                .totalAmount(booking.getTotalAmount())

                .status(booking.getStatus().name())
                .createdAt(booking.getCreatedAt().toString())
                .build();
    }


}