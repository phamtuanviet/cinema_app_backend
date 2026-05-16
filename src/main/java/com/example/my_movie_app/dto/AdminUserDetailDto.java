package com.example.my_movie_app.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Data
@Builder
public class AdminUserDetailDto {
    private UUID id;
    private String fullName;
    private String email;
    private String avatarUrl;
    private Integer availablePoints;
    private List<AdminUserVoucherDto> userVouchers;
    private List<AdminLoyaltyTransactionDto> loyaltyTransactions;
}