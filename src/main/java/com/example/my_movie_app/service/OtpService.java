package com.example.my_movie_app.service;

import com.example.my_movie_app.enums.OtpType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final long OTP_TTL = 5; // minutes

    public void generateAndSendOtp(String email, OtpType type) {

        String otp = String.valueOf(
                ThreadLocalRandom.current().nextInt(100000, 999999)
        );

        String key = buildKey(email, type);

        redisTemplate.opsForValue()
                .set(key, otp, OTP_TTL, TimeUnit.MINUTES);

        System.out.println(type + " OTP for " + email + ": " + otp);
    }

    public boolean verifyOtp(String email, String otp, OtpType type) {

        String key = buildKey(email, type);

        String storedOtp = redisTemplate.opsForValue().get(key);

        if (storedOtp == null) {
            return false;
        }

        boolean valid = storedOtp.equals(otp);

        if (valid) {
            redisTemplate.delete(key);
        }

        return valid;
    }

    private String buildKey(String email, OtpType type) {
        return "otp:" + type.name().toLowerCase() + ":" + email;
    }
}