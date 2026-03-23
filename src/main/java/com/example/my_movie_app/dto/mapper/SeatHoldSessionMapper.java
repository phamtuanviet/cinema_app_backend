package com.example.my_movie_app.dto.mapper;

import com.example.my_movie_app.dto.ComboDto;
import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.dto.ShowtimeDetailDto;
import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.dto.response.SeatHoldSessionInfoDto;
import com.example.my_movie_app.entity.Combo;
import com.example.my_movie_app.entity.LoyaltyAccount;
import com.example.my_movie_app.entity.SeatHoldSession;
import com.example.my_movie_app.entity.UserVoucher;

import java.util.List;
import java.util.stream.Collectors;

public class SeatHoldSessionMapper {

    public static SeatHoldSessionInfoDto toDto(
            SeatHoldSession session,
            List<Combo> combos,
            List<UserVoucher> userVouchers,
            LoyaltyAccount loyaltyAccount,
            Double seatAmount
    ) {

        // 🎬 movie
        MovieDto movieDto = MovieMapper.toDto(session.getShowtime().getMovie());

        // 🎥 showtime
        ShowtimeDetailDto showtimeDto =
                ShowtimeMapper.toDto(session.getShowtime());

        // 🍿 combos
        List<ComboDto> comboDtos = combos.stream()
                .map(ComboMapper::toDto)
                .collect(Collectors.toList());

        // 🎟 vouchers
        List<VoucherDto> voucherDtos = userVouchers.stream()
                .map(uv -> VoucherMapper.toDto(
                        uv.getVoucher(),
                        uv,
                        seatAmount
                ))
                .collect(Collectors.toList());

        // 💰 points
        Integer points = loyaltyAccount != null
                ? loyaltyAccount.getAvailablePoints()
                : 0;

        return SeatHoldSessionInfoDto.builder()
                .movie(movieDto)
                .showtime(showtimeDto)
                .combos(comboDtos)
                .vouchers(voucherDtos)
                .availablePoints(points)
                .seatAmount(seatAmount)
                .build();
    }
}