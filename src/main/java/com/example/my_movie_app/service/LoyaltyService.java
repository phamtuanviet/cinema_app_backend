package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.response.LoyaltyAccountResponse;
import com.example.my_movie_app.dto.response.LoyaltyTransactionResponse;
import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.LoyaltyAccount;
import com.example.my_movie_app.entity.Showtime;
import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.repository.LoyaltyAccountRepository;
import com.example.my_movie_app.repository.LoyaltyTransactionRepository;
import com.example.my_movie_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LoyaltyService {

    private final LoyaltyAccountRepository accountRepository;
    private final LoyaltyTransactionRepository transactionRepository;
    private final UserRepository userRepository;

    public LoyaltyAccountResponse getAccount(UUID userId) {
        return accountRepository.findById(userId)
                .map(account -> LoyaltyAccountResponse.builder()
                        .availablePoints(account.getAvailablePoints())
                        .build())
                .orElseGet(() -> {
                    // 1. Lấy User từ DB
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));

                    // 2. Tạo mới account và GÁN ĐỐI TƯỢNG USER
                    LoyaltyAccount newAccount = new LoyaltyAccount();
                    newAccount.setUserId(userId);
                    newAccount.setUser(user); // QUAN TRỌNG: Phải gán cái này để MapsId hoạt động
                    newAccount.setAvailablePoints(0);

                    accountRepository.save(newAccount);

                    return LoyaltyAccountResponse.builder()
                            .availablePoints(0)
                            .build();
                });
    }

    public List<LoyaltyTransactionResponse> getTransactions(UUID userId) {

        if (!accountRepository.existsById(userId)) {
            return Collections.emptyList();
        }

        return transactionRepository.findByAccountUserIdOrderByCreatedAtDesc(userId)
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