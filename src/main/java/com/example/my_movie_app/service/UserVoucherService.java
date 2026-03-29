package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.dto.response.UserVoucherResponse;
import com.example.my_movie_app.entity.*;
import com.example.my_movie_app.enums.VoucherStatus;
import com.example.my_movie_app.repository.UserVoucherRepository;
import com.example.my_movie_app.repository.VoucherRepository;
import com.example.my_movie_app.repository.VoucherUsageRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserVoucherService {

    private final UserVoucherRepository userVoucherRepository;
    private final VoucherRepository voucherRepository;
    private final VoucherUsageRepository voucherUsageRepository;

    @PersistenceContext
    private EntityManager entityManager;

    public VoucherDto addVoucher(UUID userId, String code) {

        Voucher voucher = voucherRepository.findByCode(code)
                .orElseThrow(() -> new RuntimeException("Voucher not found"));

        if (!voucher.getActive()) {
            throw new RuntimeException("Voucher not active");
        }

        if (voucher.getExpiryDate() != null &&
                voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Voucher expired");
        }

        // ❗ check đã add chưa
        boolean exists = userVoucherRepository
                .existsByUserIdAndVoucherId(userId, voucher.getId());

        if (exists) {
            throw new RuntimeException("Voucher already added");
        }

        User user = entityManager.getReference(User.class, userId);

        UserVoucher userVoucher = UserVoucher.builder()
                .user(user)
                .voucher(voucher)
                .isUsed(false)
                .build();

        userVoucherRepository.save(userVoucher);

        return mapToVoucherDto(voucher);
    }

    private VoucherDto mapToVoucherDto(Voucher v) {

        boolean isUsable = Boolean.TRUE.equals(v.getActive()) &&
                (v.getExpiryDate() == null || v.getExpiryDate().isAfter(LocalDateTime.now()));

        return VoucherDto.builder()
                .id(v.getId().toString())
                .code(v.getCode())

                .title("Voucher " + v.getCode())

                .discountType(v.getDiscountType())
                .discountValue(v.getDiscountValue())
                .maxDiscount(v.getMaxDiscount())
                .minOrderAmount(v.getMinOrderValue())

                .expiryDate(v.getExpiryDate())
                .isUsable(isUsable)

                .build();
    }

    private UserVoucherResponse mapToResponse(
            UserVoucher uv,
            Map<UUID, VoucherUsage> usageMap
    ) {

        Voucher v = uv.getVoucher();

        // 🔥 lấy từ map thay vì query DB
        VoucherUsage usage = usageMap.get(uv.getId());

        String movieTitle = null;
        String cinemaName = null;
        String roomName = null;
        LocalDateTime showtime = null;
        BigDecimal discountAmount = null;
        LocalDateTime usedAt = null;

        if (usage != null && usage.getBooking() != null) {

            Booking booking = usage.getBooking();
            Showtime st = booking.getShowtime();

            if (st != null) {
                movieTitle = st.getMovie().getTitle();
                cinemaName = st.getRoom().getCinema().getName();
                roomName = st.getRoom().getName();
                showtime = st.getStartTime();
            }

            discountAmount = usage.getDiscountAmount();
            usedAt = usage.getUsedAt();
        }

        return UserVoucherResponse.builder()
                .id(uv.getId())
                .code(v.getCode())
                .discountType(v.getDiscountType().name())
                .discountValue(v.getDiscountValue())
                .minOrderValue(v.getMinOrderValue())
                .maxDiscount(v.getMaxDiscount())
                .expiryDate(v.getExpiryDate())
                .isUsed(uv.getIsUsed())

                // usage info
                .movieTitle(movieTitle)
                .cinemaName(cinemaName)
                .roomName(roomName)
                .showtime(showtime)
                .discountAmount(discountAmount)
                .usedAt(usedAt)

                .build();
    }

    public List<UserVoucherResponse> getUserVouchers(UUID userId, VoucherStatus status) {

        List<UserVoucher> list = userVoucherRepository.findByUserId(userId);

        // 🔥 2. Lấy usage 1 lần duy nhất (QUAN TRỌNG)
        List<VoucherUsage> usages = voucherUsageRepository.findByUserId(userId);

        Map<UUID, VoucherUsage> usageMap = usages.stream()
                .collect(Collectors.toMap(
                        vu -> vu.getUserVoucher().getId(),
                        vu -> vu
                ));

        LocalDateTime now = LocalDateTime.now();

        // 3. map + filter
        return list.stream()
                .filter(uv -> filterByStatus(uv, status, now))
                .sorted(getComparator(status, usageMap)) // 👈 thêm dòng này
                .map(uv -> mapToResponse(uv, usageMap))
                .toList();
    }

    private Comparator<UserVoucher> getComparator(
            VoucherStatus status,
            Map<UUID, VoucherUsage> usageMap
    ) {

        switch (status) {

            case AVAILABLE:
                return Comparator.comparing(
                        uv -> uv.getVoucher().getExpiryDate(),
                        Comparator.nullsLast(Comparator.naturalOrder())
                );

            case USED:
                return Comparator.comparing(
                        (UserVoucher uv) -> {
                            VoucherUsage usage = usageMap.get(uv.getId());
                            return usage != null ? usage.getUsedAt() : null;
                        },
                        Comparator.nullsLast(Comparator.reverseOrder())
                );

            case EXPIRED:
                return Comparator.comparing(
                        uv -> uv.getVoucher().getExpiryDate(),
                        Comparator.nullsLast(Comparator.reverseOrder())
                );

            default:
                return (a, b) -> 0;
        }
    }

    private boolean filterByStatus(UserVoucher uv, VoucherStatus status, LocalDateTime now) {

        Voucher v = uv.getVoucher();
        LocalDateTime expiry = v.getExpiryDate();

        switch (status) {

            case AVAILABLE:
                return Boolean.FALSE.equals(uv.getIsUsed())
                        && (expiry == null || expiry.isAfter(now));

            case USED:
                return Boolean.TRUE.equals(uv.getIsUsed());

            case EXPIRED:
                return expiry != null && expiry.isBefore(now);

            default:
                return true;
        }
    }


}