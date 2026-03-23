package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.request.CreatePaymentRequest;
import com.example.my_movie_app.dto.response.CreatePaymentResponse;
import com.example.my_movie_app.entity.*;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.enums.LoyaltyTransactionType;
import com.example.my_movie_app.enums.PaymentStatus;
import com.example.my_movie_app.enums.UsageStatus;
import com.example.my_movie_app.repository.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.SessionStatus;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final VoucherUsageRepository voucherUsageRepository;
    private final LoyaltyAccountRepository loyaltyAccountRepository;
    private final LoyaltyTransactionRepository loyaltyTransactionRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public void handleVnpayCallback(Map<String, String> params) {

        String txnRef = params.get("vnp_TxnRef");
        String responseCode = params.get("vnp_ResponseCode");
        String transactionId = params.get("vnp_TransactionNo");

        Booking booking = bookingRepository.findByTicketCode(txnRef);

        if (booking == null) {
            throw new RuntimeException("Booking not found");
        }

        // 🔥 idempotent mạnh hơn
        boolean exists = paymentRepository
                .existsByGatewayOrderIdAndStatus(txnRef, PaymentStatus.SUCCESS);

        if (exists) {
            return; // đã xử lý rồi
        }

        if ("00".equals(responseCode)) {
            handleSuccess(booking, txnRef, transactionId, responseCode);
        } else {
            handleFail(booking, txnRef, transactionId, responseCode);
        }
    }

    private void handleSuccess(
            Booking booking,
            String txnRef,
            String transactionId,
            String responseCode
    ) {

        booking.setStatus(BookingStatus.PAID);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod("VNPAY");

        payment.setGatewayOrderId(txnRef);
        payment.setGatewayTransactionId(transactionId);
        payment.setGatewayResponseCode(responseCode);

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaymentTime(LocalDateTime.now());

        paymentRepository.save(payment);
    }

    private void handleFail(
            Booking booking,
            String txnRef,
            String transactionId,
            String responseCode
    ) {

        booking.setStatus(BookingStatus.CANCELLED);
        booking.setCancelledAt(LocalDateTime.now());

        // 🔥 lưu payment FAIL (rất quan trọng)
        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getTotalAmount());
        payment.setPaymentMethod("VNPAY");

        payment.setGatewayOrderId(txnRef);
        payment.setGatewayTransactionId(transactionId);
        payment.setGatewayResponseCode(responseCode);

        payment.setStatus(PaymentStatus.FAILED);
        payment.setPaymentTime(LocalDateTime.now());

        paymentRepository.save(payment);

        // =========================
        // rollback voucher
        // =========================
        List<VoucherUsage> usages = voucherUsageRepository.findByBooking(booking);

        for (VoucherUsage usage : usages) {

            usage.setStatus(UsageStatus.REFUNDED);

            UserVoucher userVoucher = usage.getUserVoucher();
            if (userVoucher != null) {
                userVoucher.setIsUsed(false);
                userVoucher.setUsedAt(null);
            }
        }

        // =========================
        // rollback point
        // =========================
        LoyaltyAccount account = loyaltyAccountRepository
                .findByUser_Id(booking.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Account not found"));

        List<LoyaltyTransaction> transactions =
                loyaltyTransactionRepository.findByBooking(booking);

        for (LoyaltyTransaction tx : transactions) {

            if (tx.getType() == LoyaltyTransactionType.REDEEM) {

                account.setAvailablePoints(
                        account.getAvailablePoints() + tx.getPoints()
                );

                LoyaltyTransaction refund = new LoyaltyTransaction();
                refund.setAccount(account);
                refund.setPoints(tx.getPoints());
                refund.setType(LoyaltyTransactionType.REFUND);
                refund.setBooking(booking);
                refund.setDescription("Refund point for failed booking");

                loyaltyTransactionRepository.save(refund);
            }
        }

        // =========================
        // release seat
        // =========================
        if (booking.getSession() != null) {
            booking.getSession().setExpiresAt(Instant.now());
        }
    }
}