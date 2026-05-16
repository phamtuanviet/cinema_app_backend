package com.example.my_movie_app.service;

import com.example.my_movie_app.dto.request.*;
import com.example.my_movie_app.dto.response.*;
import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.entity.UserDevice;
import com.example.my_movie_app.entity.UserToken;

import com.example.my_movie_app.enums.OtpType;
import com.example.my_movie_app.repository.UserDeviceRepository;
import com.example.my_movie_app.repository.UserRepository;
import com.example.my_movie_app.repository.UserTokenRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;



@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final UserTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final OtpService otpService;
    private final UserDeviceRepository userDeviceRepository;

    // ================= REGISTER =================

    public RegisterResponse register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhone(request.getPhone());
        user.setIsVerified(false);

        userRepository.save(user);

        otpService.generateAndSendOtp(user.getEmail(),OtpType.REGISTER);

        return RegisterResponse.builder()
                .message("Check your email to verify account")
                .build();    }

    // ================= VERIFY EMAIL =================

    public VerifyEmailResponse verifyEmail(VerifyEmailRequest request) {

        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpType.REGISTER);

        if (!valid) {
            throw new RuntimeException("Invalid OTP");
        }

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIsVerified(true);

        userRepository.save(user);

        return  VerifyEmailResponse.builder()
                .message("Email verified successfully")
                .build();
    }

    // ================= LOGIN =================

    @Transactional
    public LoginResponse login(
            LoginRequest request,
            String deviceInfo,
            String ipAddress
    ) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Tài khoản không tồn tại"));

        // 🔥 1. KIỂM TRA BANNED (THÊM VÀO ĐÂY)
        if (user.getIsBanned() != null && user.getIsBanned()) {
            throw new RuntimeException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ bộ phận hỗ trợ.");
        }

        // 2. Kiểm tra mật khẩu
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Mật khẩu không chính xác");
        }

        // 3. Kiểm tra xác thực email
        if (user.getIsVerified() != null && !user.getIsVerified()) {
            throw new RuntimeException("Email chưa được xác thực");
        }

        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);

        // 4. Lưu Session Token
        UserToken token = new UserToken();
        token.setUser(user);
        token.setRefreshToken(refreshToken);
        token.setExpiresAt(LocalDateTime.now().plusDays(30));
        token.setDeviceInfo(deviceInfo);
        token.setIpAddress(ipAddress);
        token.setIsRevoked(false);
        tokenRepository.save(token);

        // 5. XỬ LÝ FCM TOKEN
        String fcmToken = request.getFcmToken();
        if (fcmToken != null && !fcmToken.trim().isEmpty()) {
            UserDevice userDevice = userDeviceRepository.findByFcmToken(fcmToken)
                    .orElse(new UserDevice());

            userDevice.setUser(user);
            userDevice.setFcmToken(fcmToken);
            userDevice.setDeviceType("ANDROID");
            userDevice.setActive(true);

            userDeviceRepository.save(userDevice);
        }

        return LoginResponse.builder()
                .user(mapToUserDto(user))
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
    // ================= REFRESH TOKEN =================

    public RefreshResponse refresh(RefreshRequest request,
                                   String deviceInfo,
                                   String ipAddress) {

        UserToken token = tokenRepository
                .findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.getIsRevoked()) {
            throw new RuntimeException("Token revoked");
        }

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expired");
        }

        User user = token.getUser();

        String newAccessToken = jwtService.generateAccessToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        token.setIsRevoked(true);
        tokenRepository.save(token);

        UserToken newToken = new UserToken();
        newToken.setUser(user);
        newToken.setRefreshToken(newRefreshToken);
        newToken.setExpiresAt(LocalDateTime.now().plusDays(30));
        newToken.setDeviceInfo(deviceInfo);
        newToken.setIpAddress(ipAddress);
        newToken.setIsRevoked(false);

        tokenRepository.save(newToken);

        return RefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    // ================= LOGOUT =================

    public LogoutResponse logout(LogoutRequest request) {

        UserToken token = tokenRepository
                .findByRefreshToken(request.getRefreshToken())
                .orElseThrow(() -> new RuntimeException("Token not found"));

        token.setIsRevoked(true);

        tokenRepository.save(token);

        return LogoutResponse.builder().message("Logged out successfully").build();
    }

    // ================= FORGOT PASSWORD =================

    public ForgotPasswordResponse forgotPassword(ForgotPasswordRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        otpService.generateAndSendOtp(user.getEmail(), OtpType.FORGOT_PASSWORD);

        return ForgotPasswordResponse.builder().message("OTP sent to email").build();
    }

    // ================= VERIFY FORGOT =================

    public VerifyForgotPasswordResponse verifyForgotPassword(
            VerifyForgotPasswordRequest request
    ) {

        boolean valid = otpService.verifyOtp(request.getEmail(), request.getOtp(), OtpType.FORGOT_PASSWORD);

        if (!valid) {
            throw new RuntimeException("Invalid OTP");
        }

        String resetToken = jwtService.generateResetToken(request.getEmail());

        return  VerifyForgotPasswordResponse.builder().resetToken(resetToken).build();
    }

    // ================= RESET PASSWORD =================

    public ResetPasswordResponse resetPassword(ResetPasswordRequest request) {

        String email = jwtService.extractEmailFromResetToken(request.getResetToken());

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return ResetPasswordResponse.builder()
                .message("Password reset successfully")
                .build();
    }

    // ================= MAPPER =================

    private UserResponse mapToUserDto(User user) {

        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .phone(user.getPhone())
                .isVerified(user.getIsVerified())
                .role(user.getRole().name())
                .build();
    }
}