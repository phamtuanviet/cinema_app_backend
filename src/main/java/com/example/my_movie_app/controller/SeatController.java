package com.example.my_movie_app.controller;


import com.example.my_movie_app.config.UserPrincipal;
import com.example.my_movie_app.dto.SeatMapDto;
import com.example.my_movie_app.dto.request.CancelSeatCoupleRequest;
import com.example.my_movie_app.dto.request.CancelSeatRequest;
import com.example.my_movie_app.dto.request.HoldSeatCoupleRequest;
import com.example.my_movie_app.dto.request.HoldSeatRequest;
import com.example.my_movie_app.dto.response.CancelSeatCoupleResponse;
import com.example.my_movie_app.dto.response.CancelSeatResponse;
import com.example.my_movie_app.dto.response.HoldSeatCoupleResponse;
import com.example.my_movie_app.dto.response.HoldSeatResponse;
import com.example.my_movie_app.entity.Seat;
import com.example.my_movie_app.service.JwtService;
import com.example.my_movie_app.service.SeatService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/seat")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;
    private final JwtService jwtService;

    @GetMapping("/room/{roomId}")
    public List<Seat> getSeats(@PathVariable UUID roomId) {
        return seatService.getSeatsByRoom(roomId);
    }

    @GetMapping("/{showtimeId}/seats")
    public ResponseEntity<SeatMapDto> getSeatMap(
            @PathVariable String showtimeId,
            @AuthenticationPrincipal UserPrincipal user    ) {
        SeatMapDto result = seatService.getSeatMap(
                UUID.fromString(showtimeId),
                user.getId()
        );

        return ResponseEntity.ok(result);
    }

    @PostMapping("/cancel")
    public ResponseEntity<CancelSeatResponse> cancelSeat(
            @RequestBody CancelSeatRequest cancelSeatRequest,
            @AuthenticationPrincipal UserPrincipal user){
        CancelSeatResponse result = seatService.cancelSeat(cancelSeatRequest.getShowtimeId(),cancelSeatRequest.getSeatId(),user.getId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/hold")
    public ResponseEntity<HoldSeatResponse> cancelSeat(
            @RequestBody HoldSeatRequest holdSeatRequest,
            @AuthenticationPrincipal UserPrincipal user){
        HoldSeatResponse result = seatService.holdSeat(holdSeatRequest.getShowtimeId(),holdSeatRequest.getSeatId(),user.getId());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/hold-couple")
    public HoldSeatCoupleResponse holdCouple(@RequestBody HoldSeatCoupleRequest req, @AuthenticationPrincipal UserPrincipal user) {
        return seatService.holdSeatCouple(
                req.getShowtimeId(),
                req.getFirstSeatId(),
                req.getSecondSeatId(),
                user.getId()
        );
    }

    @PostMapping("/cancel-couple")
    public CancelSeatCoupleResponse cancelCouple(@RequestBody CancelSeatCoupleRequest req,@AuthenticationPrincipal UserPrincipal user) {
        return seatService.cancelSeatCouple(
                req.getShowtimeId(),
                req.getFirstSeatId(),
                req.getSecondSeatId(),
                user.getId()
        );
    }

}