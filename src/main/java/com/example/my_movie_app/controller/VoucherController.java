package com.example.my_movie_app.controller;

import com.example.my_movie_app.config.UserPrincipal;
import com.example.my_movie_app.dto.VoucherDto;
import com.example.my_movie_app.dto.request.AddVoucherRequest;
import com.example.my_movie_app.service.VoucherService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/voucher")
@RequiredArgsConstructor
public class VoucherController {

    private final VoucherService voucherService;

    @PostMapping
    public ResponseEntity<VoucherDto> addVoucher(
            @RequestBody AddVoucherRequest request,
            @AuthenticationPrincipal UserPrincipal user
    ) {
        VoucherDto result = voucherService.addVoucher(user.getId(), request.getCode());
        return ResponseEntity.ok(result);
    }
}
