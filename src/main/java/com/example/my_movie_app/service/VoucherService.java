package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.dto.mapper.VoucherMapper;
import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.entity.UserVoucher;
import com.example.my_movie_app.entity.Voucher;
import com.example.my_movie_app.repository.UserRepository;
import com.example.my_movie_app.repository.UserVoucherRepository;
import com.example.my_movie_app.repository.VoucherRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VoucherService {

    private final VoucherRepository voucherRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;


//    public VoucherDto addVoucher(UUID userId, String code) {
//
//        Voucher voucher = voucherRepository.findByCode(code)
//                .orElseThrow(() -> new RuntimeException("Voucher not found"));
//
//        if (!voucher.getActive()) {
//            throw new RuntimeException("Voucher not active");
//        }
//
//        if (voucher.getExpiryDate() != null &&
//                voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
//            throw new RuntimeException("Voucher expired");
//        }
//
//        // ❗ check đã add chưa
//        boolean exists = userVoucherRepository.existsByUserIdAndVoucherId(userId, voucher.getId());
//        if (exists) {
//            throw new RuntimeException("Voucher already added");
//        }
//
//        UserVoucher userVoucher = UserVoucher.builder()
//                .user(User.builder().id(userId).build()) // ok vì chỉ reference
//                .voucher(voucher)
//                .isUsed(false)
//                .build();
//
//        userVoucherRepository.save(userVoucher);
//
//        // 🔥 return DTO cho FE dùng luôn
//        return mapToVoucherDto(voucher);
//    }

//    private VoucherDto mapToVoucherDto(Voucher v) {
//
//        boolean isUsable = Boolean.TRUE.equals(v.getActive()) &&
//                (v.getExpiryDate() == null || v.getExpiryDate().isAfter(LocalDateTime.now()));
//
//        return VoucherDto.builder()
//                .id(v.getId().toString())
//                .code(v.getCode())
//
//                .title("Voucher " + v.getCode())
//                .description(null)
//
//                .discountType(v.getDiscountType())
//                .discountValue(v.getDiscountValue())
//                .maxDiscount(v.getMaxDiscount())
//                .minOrderAmount(v.getMinOrderValue())
//
//                .expiryDate(v.getExpiryDate())
//                .isUsable(isUsable)
//
//                .build();
//    }

    public List<Voucher> importFromFile(MultipartFile file) throws Exception {

        List<Voucher> vouchers = objectMapper.readValue(
                file.getInputStream(),
                new TypeReference<List<Voucher>>() {}
        );

        // set default
        vouchers.forEach(v -> {
            if (v.getUsedCount() == null) v.setUsedCount(0);
            if (v.getActive() == null) v.setActive(true);
        });

        return voucherRepository.saveAll(vouchers);
    }
}