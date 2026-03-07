package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserTokenRepository extends JpaRepository<UserToken, UUID> {

    Optional<UserToken> findByRefreshToken(String refreshToken);

}