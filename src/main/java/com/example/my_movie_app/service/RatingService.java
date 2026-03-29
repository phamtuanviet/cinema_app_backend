package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.request.RatingRequest;
import com.example.my_movie_app.dto.response.RatingResponse;
import com.example.my_movie_app.entity.Movie;
import com.example.my_movie_app.entity.Rating;
import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.repository.MovieRepository;
import com.example.my_movie_app.repository.RatingRepository;
import com.example.my_movie_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RatingService {

    private final RatingRepository ratingRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;

    public RatingResponse rateMovie(RatingRequest req, UUID userId) {

        if (req.getScore() < 1 || req.getScore() > 5) {
            throw new RuntimeException("Score must be between 1 and 5");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Movie movie = movieRepository.findById(req.getMovieId())
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        Rating rating = ratingRepository
                .findByUserIdAndMovieId(userId, req.getMovieId())
                .orElseGet(() -> {
                    Rating r = new Rating();
                    r.setUser(user);
                    r.setMovie(movie);
                    return r;
                });

        rating.setScore(req.getScore());
        ratingRepository.save(rating);

        Double avg = ratingRepository.getAverageScore(req.getMovieId());

        RatingResponse res = new RatingResponse();
        res.setMovieId(req.getMovieId());
        res.setUserScore(req.getScore());
        res.setAverageScore(avg != null ? avg : 0.0);

        return res;
    }
}
