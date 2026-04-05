package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.response.UserVoucherChatbotResponse;
import com.example.my_movie_app.dto.response.UserVoucherResponse;
import com.example.my_movie_app.entity.UserVoucher;
import com.example.my_movie_app.entity.Voucher;
import com.example.my_movie_app.enums.DiscountType;
import com.example.my_movie_app.repository.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatbotVoucherService {

    private final UserVoucherRepository userVoucherRepository;

    @Transactional(readOnly = true)
    public List<UserVoucherChatbotResponse> getMyVouchers(UUID userId, String status) {
        // Lấy toàn bộ voucher trong ví của User
        List<UserVoucher> userVouchers = userVoucherRepository.findByUserId(userId);
        LocalDateTime now = LocalDateTime.now();

        return userVouchers.stream()
                .filter(uv -> filterByStatus(uv, status, now))
                .map(this::mapToVoucherResponse)
                .collect(Collectors.toList());
    }

    private boolean filterByStatus(UserVoucher uv, String status, LocalDateTime now) {
        if ("used".equalsIgnoreCase(status)) return uv.getIsUsed();
        if ("expired".equalsIgnoreCase(status)) return !uv.getIsUsed() && uv.getVoucher().getExpiryDate().isBefore(now);

        // Mặc định lấy "active": Chưa dùng và còn hạn, và voucher gốc phải đang active
        return !uv.getIsUsed()
                && uv.getVoucher().getExpiryDate().isAfter(now)
                && uv.getVoucher().getActive();
    }

    private UserVoucherChatbotResponse mapToVoucherResponse(UserVoucher uv) {
        Voucher v = uv.getVoucher();
        String displayName = (v.getDiscountType() == DiscountType.PERCENT)
                ? "Giảm " + v.getDiscountValue() + "%"
                : "Giảm " + String.format("%,.0fđ", v.getDiscountValue());

        if (v.getMaxDiscount() != null && v.getDiscountType() == DiscountType.PERCENT) {
            displayName += " (Tối đa " + String.format("%,.0fđ", v.getMaxDiscount()) + ")";
        }

        return UserVoucherChatbotResponse.builder()
                .voucherCode(v.getCode())
                .displayName(displayName)
                .description("Áp dụng cho đơn từ " + String.format("%,.0fđ", v.getMinOrderValue()))
                .expiryDate(v.getExpiryDate())
                .status(uv.getIsUsed() ? "Đã dùng" : (v.getExpiryDate().isBefore(LocalDateTime.now()) ? "Hết hạn" : "Khả dụng"))
                .minOrderValue(v.getMinOrderValue())
                .build();
    }
}