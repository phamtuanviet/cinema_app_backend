package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.dto.mapper.VoucherMapper;
import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.entity.UserVoucher;
import com.example.my_movie_app.entity.Voucher;
import com.example.my_movie_app.repository.UserRepository;
import com.example.my_movie_app.repository.UserVoucherRepository;
import com.example.my_movie_app.repository.VoucherRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;

    @Transactional
    public VoucherDto addVoucher(UUID userId, String code) {

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher không tồn tại"));

        if (!voucher.getActive()) {
            throw new RuntimeException("Voucher đã bị vô hiệu hóa");
        }

        if (voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Voucher đã hết hạn");
        }

        if (voucher.getUsageLimit() != null &&
                voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new RuntimeException("Voucher đã hết lượt sử dụng");
        }

        boolean existed = userVoucherRepository
                .existsByUserIdAndVoucherId(userId, voucher.getId());

        if (existed) {
            throw new RuntimeException("Bạn đã sở hữu voucher này");
        }

        User user = userRepository.findById(userId)
                .orElseThrow();

        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .isUsed(false)
                .build();

        userVoucherRepository.save(userVoucher);

        return VoucherMapper.toDto(voucher, userVoucher, 0.0);
    }
}