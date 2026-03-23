package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.LoyaltyAccount;
import com.example.my_movie_app.entity.LoyaltyTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LoyaltyTransactionRepository extends JpaRepository<LoyaltyTransaction, UUID> {
    List<LoyaltyTransaction> findByBooking(Booking booking);
}

