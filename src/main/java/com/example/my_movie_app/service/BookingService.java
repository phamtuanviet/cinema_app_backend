package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.mapper.BookingMapper;
import com.example.my_movie_app.dto.request.BookingRequest;
import com.example.my_movie_app.dto.response.BookingResponse;
import com.example.my_movie_app.entity.*;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.enums.DiscountType;
import com.example.my_movie_app.enums.LoyaltyTransactionType;
import com.example.my_movie_app.enums.UsageStatus;
import com.example.my_movie_app.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BookingService {

    private final SeatHoldSessionRepository sessionRepo;
    private final SeatReservationRepository seatReservationRepo;
    private final ComboRepository comboRepo;
    private final UserVoucherRepository userVoucherRepo;
    private final LoyaltyAccountRepository loyaltyAccountRepo;
    private final LoyaltyTransactionRepository loyaltyTransactionRepo;
    private final BookingRepository bookingRepo;
    private final BookingComboRepository  bookingComboRepo;
    private final VoucherUsageRepository voucherUsageRepo;

    @Transactional
    public BookingResponse createBooking(BookingRequest req, UUID userId) {
        System.out.println("Booking");

        Instant now = Instant.now();

        // =========================
        // 🔥 1. SESSION
        // =========================
        SeatHoldSession session = sessionRepo.findById(UUID.fromString(req.getSeatHoldSessionId()))
                .orElseThrow(() -> new RuntimeException("Session not found"));

        if (session.getExpiresAt().isBefore(now)) {
            throw new RuntimeException("Session expired");
        }

        if (!session.getUser().getId().equals(userId)) {
            throw new RuntimeException("Invalid session");
        }


        int updated = sessionRepo.extendSession(
                session.getId(),
                5 // phút
        );

        if (updated == 0) {
            throw new RuntimeException("Session expired (cannot extend)");
        }

        Showtime showtime = session.getShowtime();

        // =========================
        // 🔥 2. SEATS
        // =========================
        List<SeatReservation> reservations =
                seatReservationRepo.findBySession_IdAndIsCancelFalse(session.getId());

        if (reservations.isEmpty()) {
            throw new RuntimeException("No seats selected");
        }

        // =========================
        // 🔥 3. SEAT AMOUNT
        // =========================
        BigDecimal seatAmount = reservations.stream()
                .map(r -> showtime.getBasePrice().add(r.getSeat().getPriceModifier()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // =========================
        // 🔥 4. COMBO
        // =========================
        BigDecimal comboAmount = BigDecimal.ZERO;

        if (req.getSelectedCombos() != null) {
            for (Map.Entry<String, Integer> entry : req.getSelectedCombos().entrySet()) {

                Combo combo = comboRepo.findById(UUID.fromString(entry.getKey()))
                        .orElseThrow(() -> new RuntimeException("Combo not found"));

                BigDecimal price = BigDecimal.valueOf(combo.getPrice())
                        .multiply(BigDecimal.valueOf(entry.getValue()));

                comboAmount = comboAmount.add(price);
            }
        }

        // =========================
        // 🔥 5. VOUCHER (chưa update DB)
        // =========================
        BigDecimal voucherDiscount = BigDecimal.ZERO;
        UserVoucher userVoucher = null;

        if (req.getVoucherId() != null) {

            userVoucher = userVoucherRepo
                    .findByUser_IdAndVoucher_IdAndIsUsedFalse(
                            userId,
                            UUID.fromString(req.getVoucherId())
                    )
                    .orElseThrow(() -> new RuntimeException("Voucher not available"));

            Voucher voucher = userVoucher.getVoucher();

            // 🔥 check active
            if (!voucher.getActive()) {
                throw new RuntimeException("Voucher inactive");
            }

            // 🔥 check expire
            if (voucher.getExpiryDate() != null &&
                    voucher.getExpiryDate().isBefore(LocalDateTime.now())) {
                throw new RuntimeException("Voucher expired");
            }

            // 🔥 check usage limit
            if (voucher.getUsageLimit() != null &&
                    voucher.getUsedCount() >= voucher.getUsageLimit()) {
                throw new RuntimeException("Voucher usage limit reached");
            }

            // 🔥 check min order
            BigDecimal orderAmount = seatAmount.add(comboAmount);

            if (voucher.getMinOrderValue() != null &&
                    orderAmount.compareTo(voucher.getMinOrderValue()) < 0) {
                throw new RuntimeException("Not enough amount to use voucher");
            }

            // =========================
            // 🔥 CALCULATE DISCOUNT
            // =========================
            if (voucher.getDiscountType() == DiscountType.FIXED) {

                voucherDiscount = voucher.getDiscountValue();

            } else if (voucher.getDiscountType() == DiscountType.PERCENT) {

                voucherDiscount = orderAmount
                        .multiply(voucher.getDiscountValue())
                        .divide(BigDecimal.valueOf(100));

                // 🔥 apply maxDiscount
                if (voucher.getMaxDiscount() != null &&
                        voucherDiscount.compareTo(voucher.getMaxDiscount()) > 0) {
                    voucherDiscount = voucher.getMaxDiscount();
                }
            }
        }

        // =========================
        // 🔥 6. POINT (chưa update DB)
        // =========================
        BigDecimal pointDiscount = BigDecimal.ZERO;
        LoyaltyAccount account = null;

        if (req.getUsedPoints() != null && req.getUsedPoints() > 0) {

            account = loyaltyAccountRepo.findByUser_Id(userId)
                    .orElseThrow(() -> new RuntimeException("Loyalty account not found"));

            if (account.getAvailablePoints() < req.getUsedPoints()) {
                throw new RuntimeException("Not enough points");
            }

            pointDiscount = BigDecimal.valueOf(req.getUsedPoints());
        }

        // =========================
        // 🔥 7. TOTAL
        // =========================
        BigDecimal totalAmount = seatAmount
                .add(comboAmount)
                .subtract(voucherDiscount)
                .subtract(pointDiscount);

        if (totalAmount.compareTo(BigDecimal.ZERO) < 0) {
            totalAmount = BigDecimal.ZERO;
        }

        // =========================
        // 🔥 8. CREATE BOOKING
        // =========================
        Booking booking = Booking.builder()
                .user(session.getUser())
                .showtime(showtime)
                .ticketCode(generateTicketCode())
                .seatAmount(seatAmount)
                .comboAmount(comboAmount)
                .voucherDiscount(voucherDiscount)
                .pointDiscount(pointDiscount)
                .totalAmount(totalAmount)
                .status(BookingStatus.PENDING)
                .session(session)
                .build();
        bookingRepo.save(booking);

        if (req.getSelectedCombos() != null) {

            for (Map.Entry<String, Integer> entry : req.getSelectedCombos().entrySet()) {

                UUID comboId = UUID.fromString(entry.getKey());
                Integer quantity = entry.getValue();

                Combo combo = comboRepo.findById(comboId)
                        .orElseThrow(() -> new RuntimeException("Combo not found"));

                BookingCombo bookingCombo = new BookingCombo();

                // 🔥 set ID
                BookingComboId id = new BookingComboId();
                id.setBookingId(booking.getId());
                id.setComboId(comboId);

                bookingCombo.setId(id);

                // 🔥 mapping
                bookingCombo.setBooking(booking);
                bookingCombo.setCombo(combo);

                bookingCombo.setQuantity(quantity);

                // 🔥 lưu giá tại thời điểm mua (rất quan trọng)
                bookingCombo.setPrice(BigDecimal.valueOf(combo.getPrice()));

                bookingComboRepo.save(bookingCombo);
            }
        }

        // =========================
        // 🔥 9. UPDATE VOUCHER
        // =========================
        if (userVoucher != null) {

            userVoucher.setIsUsed(true);
            userVoucher.setUsedAt(LocalDateTime.now());
            userVoucherRepo.save(userVoucher);

            // 🔥 CREATE VoucherUsage
            VoucherUsage usage = new VoucherUsage();
            usage.setId(UUID.randomUUID());
            usage.setVoucher(userVoucher.getVoucher());
            usage.setUser(session.getUser());
            usage.setBooking(booking);
            usage.setUserVoucher(userVoucher);
            usage.setDiscountAmount(voucherDiscount);
            usage.setStatus(UsageStatus.USED); // 🔥 QUAN TRỌNG
            usage.setUsedAt(LocalDateTime.now());

            voucherUsageRepo.save(usage);
        }

        // =========================
        // 🔥 10. UPDATE POINT
        // =========================
        if (account != null && req.getUsedPoints() != null && req.getUsedPoints() > 0) {

            account.setAvailablePoints(account.getAvailablePoints() - req.getUsedPoints());
            loyaltyAccountRepo.save(account);

            LoyaltyTransaction transaction = LoyaltyTransaction.builder()
                    .account(account)
                    .points(-req.getUsedPoints())
                    .type(LoyaltyTransactionType.REDEEM)
                    .description("Use points for booking " + booking.getTicketCode())
                    .booking(booking)
                    .build();

            loyaltyTransactionRepo.save(transaction);
        }


        return BookingMapper.toResponse(booking);
    }


    private String generateTicketCode() {
        return "TICKET-" + System.currentTimeMillis();
    }
}