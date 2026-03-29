package com.example.my_movie_app.controller;

import com.example.my_movie_app.config.UserPrincipal;
import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.dto.response.UserVoucherResponse;
import com.example.my_movie_app.enums.VoucherStatus;
import com.example.my_movie_app.service.UserVoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user-voucher")
@RequiredArgsConstructor
public class UserVoucherController {

    private final UserVoucherService service;

    @PostMapping
    public ResponseEntity<VoucherDto> addVoucher(

            @RequestParam String code,
            @AuthenticationPrincipal UserPrincipal user
    ) {

        return ResponseEntity.ok(service.addVoucher(user.getId(), code));
    }

    @GetMapping
    public ResponseEntity<List<UserVoucherResponse>> getVouchers(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestParam VoucherStatus status
    ) {
        return ResponseEntity.ok(service.getUserVouchers(user.getId(), status));
    }
}