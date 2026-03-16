package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.request.CreatePaymentRequest;
import com.example.my_movie_app.dto.response.CreatePaymentResponse;
import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.entity.Payment;
import com.example.my_movie_app.enums.PaymentStatus;
import com.example.my_movie_app.repository.BookingRepository;
import com.example.my_movie_app.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final VnpayService vnpayService;

    public CreatePaymentResponse createPayment(CreatePaymentRequest request){

        Booking booking = bookingRepository
                .findById(request.getBookingId())
                .orElseThrow();

        Payment payment = Payment.builder()
                .booking(booking)
                .amount(booking.getTotalAmount())
                .paymentMethod(request.getPaymentMethod())
                .status(PaymentStatus.PENDING)
                .paymentTime(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);

        String paymentUrl;

        switch (request.getPaymentMethod()) {

            case "VNPAY":
                paymentUrl = vnpayService
                        .createPaymentUrl(booking,payment);
                break;

            case "ZALOPAY":
                throw new RuntimeException("Not implemented yet");

            default:
                throw new RuntimeException("Unsupported payment method");
        }

        return CreatePaymentResponse.builder()
                .paymentMethod(request.getPaymentMethod())
                .paymentUrl(paymentUrl)
                .message("Payment created")
                .build();
    }
}