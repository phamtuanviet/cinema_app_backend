package com.example.my_movie_app.dto.mapper;
import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.entity.UserVoucher;
import com.example.my_movie_app.entity.Voucher;

import java.time.LocalDateTime;

public class VoucherMapper {

    public static VoucherDto toDto(Voucher voucher, UserVoucher userVoucher, Double seatAmount) {

        boolean isUsable =
                !userVoucher.getIsUsed()
                        && voucher.getActive()
                        && voucher.getExpiryDate().isAfter(LocalDateTime.now())
                        && seatAmount >= voucher.getMinOrderValue().doubleValue();

        return VoucherDto.builder()
                .id(voucher.getId().toString())
                .code(voucher.getCode())
                .title(voucher.getCode()) // hoặc custom title
                .discountType(voucher.getDiscountType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscount(voucher.getMaxDiscount())
                .minOrderAmount(voucher.getMinOrderValue())
                .expiryDate(voucher.getExpiryDate())
                .isUsable(isUsable)
                .build();
    }
}