package com.example.my_movie_app.repository;

import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.Payment;
import com.example.my_movie_app.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {
    boolean existsByGatewayOrderIdAndStatus(
            String gatewayOrderId,
            PaymentStatus status
    );

    // 🔥 lấy payment theo booking
    List<Payment> findByBooking(Booking booking);

    // 🔥 lấy payment thành công của booking
    Optional<Payment> findByBookingAndStatus(
            Booking booking,
            PaymentStatus status
    );

    // 🔥 tìm theo txnRef (vnp_TxnRef)
    Optional<Payment> findByGatewayOrderId(String gatewayOrderId);
}