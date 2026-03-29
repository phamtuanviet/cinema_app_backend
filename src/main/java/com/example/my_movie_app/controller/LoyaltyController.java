package com.example.my_movie_app.controller;

import com.example.my_movie_app.config.UserPrincipal;
import com.example.my_movie_app.dto.response.LoyaltyAccountResponse;
import com.example.my_movie_app.dto.response.LoyaltyTransactionResponse;
import com.example.my_movie_app.service.LoyaltyService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/loyalty")
@RequiredArgsConstructor
public class LoyaltyController {

    private final LoyaltyService service;

    @GetMapping("/account")
    public LoyaltyAccountResponse getAccount( @AuthenticationPrincipal UserPrincipal user ) {
        return service.getAccount(user.getId());
    }

    @GetMapping("/transactions")
    public List<LoyaltyTransactionResponse> getTransactions(@AuthenticationPrincipal UserPrincipal user) {
        return service.getTransactions(user.getId());
    }
}