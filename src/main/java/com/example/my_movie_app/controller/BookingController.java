package com.example.my_movie_app.controller;


import com.example.my_movie_app.config.UserPrincipal;
import com.example.my_movie_app.dto.request.BookingRequest;
import com.example.my_movie_app.dto.response.BookingResponse;
import com.example.my_movie_app.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/booking")
@RequiredArgsConstructor
public class BookingController {
    private final BookingService bookingService;

    @PostMapping()
    public BookingResponse createBooking (@RequestBody BookingRequest req
            ,@AuthenticationPrincipal UserPrincipal user) {
        return bookingService.createBooking(req, user.getId());
    }
}
