package com.example.my_movie_app.service;

import com.example.my_movie_app.enums.OtpType;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.base-url}")
    private String baseUrl;

    @Async
    public void sendOtpEmail(String to, String otp, OtpType type) {

        try {

            String subject;
            String html;

            if (type == OtpType.REGISTER) {

                subject = "Verify your account";

                html = """
                        <h2>Welcome to Movie Booking</h2>
                        <p>Your OTP to verify your account is:</p>
                        <h1>%s</h1>
                        <p>This OTP expires in 5 minutes.</p>
                        """.formatted(otp);

            } else {

                subject = "Reset your password";

                html = """
                        <h2>Password Reset Request</h2>
                        <p>Your OTP to reset your password is:</p>
                        <h1>%s</h1>
                        <p>This OTP expires in 5 minutes.</p>
                        """.formatted(otp);
            }

            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(mimeMessage);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send email");
        }
    }
}