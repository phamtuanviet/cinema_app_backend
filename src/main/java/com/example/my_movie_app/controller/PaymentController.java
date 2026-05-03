package com.example.my_movie_app.controller;

import com.example.my_movie_app.config.UserPrincipal;
import com.example.my_movie_app.dto.request.CreatePaymentRequest;
import com.example.my_movie_app.dto.request.RegisterRequest;
import com.example.my_movie_app.dto.response.CreatePaymentResponse;
import com.example.my_movie_app.dto.response.RegisterResponse;
import com.example.my_movie_app.dto.response.VnpayRefundResponse;
import com.example.my_movie_app.service.PaymentService;
import com.example.my_movie_app.service.VnpayService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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

    @PostMapping("/refund/{bookingId}")
    public ResponseEntity<?> processRefund(
            @PathVariable UUID bookingId,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            HttpServletRequest request
    ) {
        try {

            String ipAddress = getClientIpAddress(request);

            String createBy = userPrincipal.getEmail();
            System.out.println("CAM ON ANH DO MIXI");

            VnpayRefundResponse response = vnpayService.refundTransaction(bookingId, ipAddress, createBy);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedForHeader = request.getHeader("X-Forwarded-For");

        if (xForwardedForHeader == null || xForwardedForHeader.isEmpty() || "unknown".equalsIgnoreCase(xForwardedForHeader)) {
            return request.getRemoteAddr();
        }

        return xForwardedForHeader.split(",")[0].trim();
    }
}