package com.example.my_movie_app.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CreatePaymentRequest {
    private UUID bookingId;

    private String paymentMethod;
}