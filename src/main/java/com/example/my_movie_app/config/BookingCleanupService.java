package com.example.my_movie_app.config;

import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.LoyaltyTransaction;
import com.example.my_movie_app.entity.UserVoucher;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.enums.LoyaltyTransactionType;
import com.example.my_movie_app.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingCleanupService {

    private final BookingRepository bookingRepo;
    private final UserVoucherRepository userVoucherRepo;
    private final LoyaltyAccountRepository loyaltyAccountRepo;
    private final LoyaltyTransactionRepository loyaltyTransactionRepo;
    private final VoucherUsageRepository voucherUsageRepo;

    @Scheduled(fixedRate = 60000) // Chạy mỗi phút
    @Transactional
    public void cleanupExpiredBookings() {
        Instant now = Instant.now();

        // Tìm các booking PENDING có session đã hết hạn
        List<Booking> expiredBookings = bookingRepo.findByStatusAndSession_ExpiresAtBefore(
                BookingStatus.PENDING,
                now
        );

        for (Booking booking : expiredBookings) {
            cancelBookingAndRefund(booking);
        }
    }

    private void cancelBookingAndRefund(Booking booking) {
        // 1. Chuyển trạng thái Booking sang CANCELLED
        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());
        bookingRepo.save(booking);

        // 2. Hoàn trả Voucher (nếu có)
        // Tìm bản ghi VoucherUsage để biết voucher nào đã dùng
        voucherUsageRepo.findByBooking_Id(booking.getId()).ifPresent(usage -> {
            UserVoucher userVoucher = usage.getUserVoucher();
            userVoucher.setIsUsed(false);
            userVoucher.setUsedAt(null);
            userVoucherRepo.save(userVoucher);

            voucherUsageRepo.delete(usage);
        });

        if (booking.getPointDiscount() != null && booking.getPointDiscount().compareTo(BigDecimal.ZERO) > 0) {
            loyaltyAccountRepo.findByUser_Id(booking.getUser().getId()).ifPresent(account -> {
                int pointsToRefund = booking.getPointDiscount().intValue();

                account.setAvailablePoints(account.getAvailablePoints() + pointsToRefund);
                loyaltyAccountRepo.save(account);

                LoyaltyTransaction refund = LoyaltyTransaction.builder()
                        .account(account)
                        .points(pointsToRefund)
                        .type(LoyaltyTransactionType.REFUND)
                        .description("Refund points for expired booking " + booking.getTicketCode())
                        .booking(booking)
                        .build();
                loyaltyTransactionRepo.save(refund);
            });
        }
    }
}