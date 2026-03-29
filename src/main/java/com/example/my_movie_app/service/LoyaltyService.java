package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.response.LoyaltyAccountResponse;
import com.example.my_movie_app.dto.response.LoyaltyTransactionResponse;
import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.LoyaltyAccount;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.repository.LoyaltyAccountRepository;
import com.example.my_movie_app.repository.LoyaltyTransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;

    public LoyaltyAccountResponse getAccount(UUID userId) {

        LoyaltyAccount account = accountRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return LoyaltyAccountResponse.builder()
                .availablePoints(account.getAvailablePoints())
                .build();
    }

    public List<LoyaltyTransactionResponse> getTransactions(UUID userId) {

        return transactionRepository.findByAccountUserId(userId)
                .stream()
                .map(tx -> {

                    Booking booking = tx.getBooking();

                    String movieTitle = null;
                    String cinemaName = null;
                    LocalDateTime showtime = null;

                    if (booking != null && booking.getShowtime() != null) {
                        Showtime st = booking.getShowtime();

                        movieTitle = st.getMovie().getTitle();
                        cinemaName = st.getRoom().getCinema().getName();
                        showtime = st.getStartTime();
                    }

                    return LoyaltyTransactionResponse.builder()
                            .points(tx.getPoints())
                            .type(tx.getType().name())
                            .description(tx.getDescription())
                            .movieTitle(movieTitle)
                            .cinemaName(cinemaName)
                            .showtime(showtime)
                            .createdAt(tx.getCreatedAt())
                            .build();
                })
                .toList();
    }
}