package com.example.my_movie_app.controller;

import com.example.my_movie_app.dto.request.CreatePaymentRequest;
import com.example.my_movie_app.dto.request.RegisterRequest;
import com.example.my_movie_app.dto.response.CreatePaymentResponse;
import com.example.my_movie_app.dto.response.RegisterResponse;
import com.example.my_movie_app.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/")
    public ResponseEntity<CreatePaymentResponse> register(
            @RequestBody CreatePaymentRequest request
    ) {
        CreatePaymentResponse response = paymentService.createPayment(request);
        return ResponseEntity.ok(response);
    }
}