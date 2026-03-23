package com.example.my_movie_app.controller;


import com.example.my_movie_app.entity.Booking;
import com.example.my_movie_app.enums.BookingStatus;
import com.example.my_movie_app.service.PaymentService;
import com.example.my_movie_app.service.VnpayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/vnpay")
@RequiredArgsConstructor
public class VnpayController {
    private final VnpayService vnpayService;
    private final PaymentService paymentService;

    @GetMapping("/ipn")
    public ResponseEntity<Map<String, String>> ipn(
            @RequestParam Map<String, String> params
    ) {

        Map<String, String> response = new HashMap<>();

        boolean valid = vnpayService.verify(new HashMap<>(params));

        if (!valid) {
            response.put("RspCode", "97");
            response.put("Message", "Invalid checksum");
            return ResponseEntity.ok(response);
        }

        paymentService.handleVnpayCallback(params);

        response.put("RspCode", "00");
        response.put("Message", "Confirm Success");

        return ResponseEntity.ok(response);
    }
}