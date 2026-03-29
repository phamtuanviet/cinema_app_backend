package com.example.my_movie_app.controller;


import com.example.my_movie_app.config.UserPrincipal;
import com.example.my_movie_app.dto.request.ChangePasswordRequest;
import com.example.my_movie_app.dto.response.ChangePasswordResponse;
import com.example.my_movie_app.dto.response.UserResponse;
import com.example.my_movie_app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @PutMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse updateProfile(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestPart("fullName") String fullName,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar
    ) {
        return userService.updateProfile(user.getId(), fullName, avatar);
    }

    @PutMapping("/me/password")
    public ChangePasswordResponse changePassword(
            @AuthenticationPrincipal UserPrincipal user,
            @RequestBody ChangePasswordRequest request
    ) {
        return userService.changePassword(user.getId(), request);

    }

}
