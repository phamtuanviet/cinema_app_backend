package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.request.CreatePaymentRequest;
import com.example.my_movie_app.dto.request.RegisterRequest;
import com.example.my_movie_app.dto.response.CreatePaymentResponse;
import com.example.my_movie_app.dto.response.RegisterResponse;
import com.example.my_movie_app.service.PaymentService;
import com.example.my_movie_app.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;
    private final VnpayService vnpayService;

    @PostMapping
    public ResponseEntity<CreatePaymentResponse> register(
            @RequestBody CreatePaymentRequest requestBody,
            HttpServletRequest request
    ) {

        if ("VNPAY".equals(requestBody.getPaymentMethod())) {

            String paymentUrl = vnpayService
                    .createPaymentUrl(requestBody.getBookingId(), request);

            CreatePaymentResponse response = CreatePaymentResponse.builder()
                    .paymentUrl(paymentUrl)
                    .paymentMethod("VNPAY")
                    .build();

            return ResponseEntity.ok(response);
        }

        throw new RuntimeException("Unsupported payment method");
    }
}