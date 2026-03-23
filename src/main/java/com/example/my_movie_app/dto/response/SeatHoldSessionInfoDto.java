package com.example.my_movie_app.dto.response;

import com.example.my_movie_app.dto.ComboDto;
import com.example.my_movie_app.dto.MovieDto;
import com.example.my_movie_app.dto.ShowtimeDetailDto;
import com.example.my_movie_app.dto.VoucherDto;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SeatHoldSessionInfoDto {

    private MovieDto movie;
    private ShowtimeDetailDto showtime;
    private List<ComboDto> combos;
    private List<VoucherDto> vouchers;
    private Integer availablePoints;
    private Double seatAmount;
}