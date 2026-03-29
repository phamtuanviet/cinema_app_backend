package com.example.my_movie_app.service;


import com.example.my_movie_app.dto.request.ChangePasswordRequest;
import com.example.my_movie_app.dto.response.ChangePasswordResponse;
import com.example.my_movie_app.dto.response.UserResponse;
import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CloudinaryService cloudinaryService;
    private final PasswordEncoder passwordEncoder;

    public UserResponse mapToResponse(User user) {
        if (user == null) return null;

        UserResponse response = new UserResponse();

        response.setId(user.getId()); // từ BaseEntity
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhone(user.getPhone());
        response.setAvatarUrl(user.getAvatarUrl());
        response.setIsVerified(user.getIsVerified());

        response.setRole(user.getRole() != null ? user.getRole().name() : null);

        return response;
    }


    public UserResponse updateProfile(UUID userId, String fullName, MultipartFile avatar) {

        if ((fullName == null || fullName.isBlank()) && (avatar == null || avatar.isEmpty())) {
            throw new RuntimeException("At least one field (fullName or avatar) must be provided");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // update fullName nếu có
        if (fullName != null && !fullName.isBlank()) {
            user.setFullName(fullName);
        }

        // update avatar nếu có
        if (avatar != null && !avatar.isEmpty()) {
            String avatarUrl = cloudinaryService.uploadImage(avatar);
            user.setAvatarUrl(avatarUrl);
        }

        userRepository.save(user);

        return mapToResponse(user);
    }

    public ChangePasswordResponse changePassword(UUID userId, ChangePasswordRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // check old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Old password is incorrect");
        }

        // encode new password (giống resetPassword)
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        return ChangePasswordResponse.builder()
                .message("Password changed successfully")
                .build();
    }
}
