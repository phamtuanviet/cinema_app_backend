package com.example.my_movie_app.service;


import com.example.my_movie_app.dto.*;
import com.example.my_movie_app.entity.LoyaltyAccount;
import com.example.my_movie_app.entity.LoyaltyTransaction;
import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.entity.UserVoucher;
import com.example.my_movie_app.enums.Role;
import com.example.my_movie_app.repository.LoyaltyAccountRepository;
import com.example.my_movie_app.repository.LoyaltyTransactionRepository;
import com.example.my_movie_app.repository.UserRepository;
import com.example.my_movie_app.repository.UserVoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final UserVoucherRepository userVoucherRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

    public AdminUserDetailDto getUserDetail(UUID userId) {
        // 1. Lấy thông tin User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        // 2. Lấy thông tin Điểm thưởng (Loyalty Account)
        // Nếu User chưa có LoyaltyAccount, mặc định điểm = 0
        LoyaltyAccount loyaltyAccount = loyaltyAccountRepository.findById(userId).orElse(null);
        int availablePoints = loyaltyAccount != null ? loyaltyAccount.getAvailablePoints() : 0;

        // 3. Lấy danh sách Voucher của User
        List<UserVoucher> userVouchers = userVoucherRepository.findByUserId(userId);
        List<AdminUserVoucherDto> userVoucherDtos = userVouchers.stream().map(uv -> {
            String usedAtStr = uv.getUsedAt() != null ? uv.getUsedAt().format(FORMATTER) : null;
            return AdminUserVoucherDto.builder()
                    .id(uv.getId())
                    .voucherCode(uv.getVoucher().getCode())
                    .discountType(uv.getVoucher().getDiscountType().name())
                    // Tùy kiểu dữ liệu BigDecimal hay Double mà bạn ép kiểu cho phù hợp
                    .discountValue(uv.getVoucher().getDiscountValue().doubleValue())
                    .isUsed(uv.getIsUsed())
                    .usedAt(usedAtStr)
                    .build();
        }).collect(Collectors.toList());

        // 4. Lấy danh sách Lịch sử giao dịch Điểm (Transactions)
        List<LoyaltyTransaction> transactions = loyaltyAccount != null ?
                loyaltyTransactionRepository.findByAccountUserIdOrderByCreatedAtDesc(userId) :
                Collections.emptyList();

        List<AdminLoyaltyTransactionDto> transactionDtos = transactions.stream().map(tx -> {
            String createdAtStr = tx.getCreatedAt() != null ? tx.getCreatedAt().format(FORMATTER) : null;
            return AdminLoyaltyTransactionDto.builder()
                    .id(tx.getId())
                    .points(tx.getPoints())
                    .type(tx.getType().name())
                    .description(tx.getDescription())
                    .createdAt(createdAtStr)
                    .build();
        }).collect(Collectors.toList());

        // 5. Gộp vào DTO Tổng
        return AdminUserDetailDto.builder()
                .id(user.getId())
                .fullName(user.getFullName()) // Tùy thuộc vào Entity User của bạn
                .email(user.getEmail())
                .avatarUrl(user.getAvatarUrl())
                .availablePoints(availablePoints)
                .userVouchers(userVoucherDtos)
                .loyaltyTransactions(transactionDtos)
                .build();
    }

    public AdminPaginatedResponse<AdminUserDto> getUsers(String search, String roleStr, int page, int size) {

        // 1. Xử lý chuỗi tìm kiếm ngay trên Java để tránh lỗi Database
        String searchParam = null;
        if (search != null && !search.trim().isEmpty()) {
            searchParam = "%" + search.trim().toLowerCase() + "%";
        }

        // 2. Chuyển String Role thành Enum Role an toàn (Nếu lỗi thì mặc định là USER)
        Role targetRole;
        try {
            targetRole = Role.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            targetRole = Role.USER;
        }

        // 3. Phân trang và sắp xếp (Sắp xếp theo email A-Z)
        // Nếu BaseEntity của bạn có trường createdAt, bạn có thể đổi thành Sort.by(Sort.Direction.DESC, "createdAt")
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "email"));

        // 4. Gọi DB
        Page<User> userPage = userRepository.searchUsersByRole(searchParam, targetRole, pageable);

        // 5. Ánh xạ Entity -> DTO
        List<AdminUserDto> dtoList = userPage.getContent().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        // 6. Đóng gói chuẩn Response
        return new AdminPaginatedResponse<>(
                dtoList,
                userPage.getNumber(),
                userPage.getTotalPages(),
                userPage.getTotalElements(),
                userPage.isLast()
        );
    }

    private AdminUserDto mapToDto(User user) {
        return AdminUserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isVerified(user.getIsVerified())
                .role(user.getRole() != null ? user.getRole().name() : "USER")
                .avatarUrl(user.getAvatarUrl())
                .isBanned(user.getIsBanned())
                .build();
    }

    @Transactional
    public AdminUserDto updateUser(UUID id, AdminUserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy Người dùng"));

        // Cập nhật 3 trường
        try {
            user.setRole(Role.valueOf(request.getRole().toUpperCase()));
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Role không hợp lệ");
        }

        user.setIsBanned(request.getIsBanned());
        user.setIsVerified(request.getIsVerified());

        User savedUser = userRepository.save(user);
        return mapToDto(savedUser); // Nhớ bổ sung .isBanned(user.getIsBanned()) vào hàm mapToDto nhé!
    }
}