package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.mapper.SeatHoldSessionMapper;
import com.example.my_movie_app.dto.response.SeatHoldSessionInfoDto;
import com.example.my_movie_app.entity.*;
import com.example.my_movie_app.repository.ComboRepository;
import com.example.my_movie_app.repository.LoyaltyAccountRepository;
import com.example.my_movie_app.repository.SeatHoldSessionRepository;
import com.example.my_movie_app.repository.UserVoucherRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SeatHoldSessionService {
    private final SeatHoldSessionRepository seatHoldSessionRepository;
    private final ComboRepository comboRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;

    @Transactional
    public SeatHoldSessionInfoDto getSeatHoldSessionInfo(UUID sessionId) {

        SeatHoldSession session = seatHoldSessionRepository
                .findByIdWithFullData(sessionId)
                .orElseThrow(() -> new RuntimeException("Session not found"));

        UUID userId = session.getUser().getId();

        // ✅ LOAD genres riêng (fix lỗi)
        session.getShowtime().getMovie().getGenres().size();

        List<Combo> combos = comboRepository.findByIsActiveTrue();

        List<UserVoucher> userVouchers =
                userVoucherRepository.findByUserId(userId);

        LoyaltyAccount loyaltyAccount =
                loyaltyAccountRepository.findById(userId).orElse(null);

        double seatAmount = session.getSeatReservations().stream()
                .mapToDouble(r -> {
                    Seat seat = r.getSeat();
                    Showtime showtime = session.getShowtime();

                    return showtime.getBasePrice()
                            .add(seat.getPriceModifier())
                            .doubleValue();
                })
                .sum();

        return SeatHoldSessionMapper.toDto(
                session,
                combos,
                userVouchers,
                loyaltyAccount,
                seatAmount
        );
    }

}
