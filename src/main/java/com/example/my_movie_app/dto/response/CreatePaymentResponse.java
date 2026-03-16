package com.example.my_movie_app.dto.response;


import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreatePaymentResponse {

    private String paymentUrl;

    private String paymentMethod;

    private String message;
}