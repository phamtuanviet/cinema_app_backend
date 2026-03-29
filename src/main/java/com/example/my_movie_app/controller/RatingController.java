package com.example.my_movie_app.controller;

import com.example.my_movie_app.config.UserPrincipal;
import com.example.my_movie_app.dto.request.RatingRequest;
import com.example.my_movie_app.dto.response.RatingResponse;
import com.example.my_movie_app.service.RatingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rating")
@RequiredArgsConstructor
public class RatingController {

    private final RatingService ratingService;

    @PostMapping
    public ResponseEntity<RatingResponse> rateMovie(
            @RequestBody RatingRequest req,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        System.out.println(req.getMovieId());
        System.out.println(req.getScore());
        return ResponseEntity.ok(
                ratingService.rateMovie(req, user.getId())
        );
    }
}