package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.response.SeatHoldSessionInfoDto;
import com.example.my_movie_app.service.SeatHoldSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/seat-hold-session")
@RequiredArgsConstructor
public class SeatHoldSessionController {

    private final SeatHoldSessionService seatHoldSessionService;


    @GetMapping("/{id}/info")
    public ResponseEntity<SeatHoldSessionInfoDto> getSeatHoldSessionInfo(
            @PathVariable("id") UUID seatHoldSessionId
    ) {
        System.out.println("Token seatHold: " + seatHoldSessionId);
        SeatHoldSessionInfoDto response =
                seatHoldSessionService.getSeatHoldSessionInfo(seatHoldSessionId);

        return ResponseEntity.ok(response);
    }
}