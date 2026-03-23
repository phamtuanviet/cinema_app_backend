package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Genre;
import com.example.my_movie_app.entity.LoyaltyAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;


@Repository
public interface LoyaltyAccountRepository extends JpaRepository<LoyaltyAccount, UUID> {

    Optional<LoyaltyAccount> findByUser_Id(UUID userId);
}

