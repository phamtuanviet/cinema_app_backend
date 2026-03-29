package com.example.my_movie_app.controller;

import com.example.my_movie_app.config.UserPrincipal;
import com.example.my_movie_app.dto.response.VoucherUsageResponse;
import com.example.my_movie_app.service.VoucherUsageService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/voucher-usage")
@RequiredArgsConstructor
public class VoucherUsageController {

    private final VoucherUsageService service;

//    @GetMapping
//    public List<VoucherUsageResponse> getUsage(
//            @AuthenticationPrincipal UserPrincipal user
//    ) {
//        return service.getUsageByUser(user.getId());
//    }
}
