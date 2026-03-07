package com.example.my_movie_app.service;

import com.example.my_movie_app.entity.User;
import com.example.my_movie_app.exception.TokenExpiredException;
import com.example.my_movie_app.exception.TokenInvalidException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Service
public class JwtService {

    private static final String SECRET =
            "my-super-secret-key-my-super-secret-key";

    private static final long ACCESS_EXP = 1000 * 60 * 15; // 15 min
    private static final long REFRESH_EXP = 1000L * 60 * 60 * 24 * 30; // 30 days
    private static final long RESET_EXP = 1000 * 60 * 10; // 10 min

    private Key getKey() {
        return Keys.hmacShaKeyFor(
                SECRET.getBytes(StandardCharsets.UTF_8)
        );
    }


    public String generateAccessToken(User user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_EXP))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateRefreshToken(User user) {

        return Jwts.builder()
                .setSubject(user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_EXP))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String generateResetToken(String email) {

        return Jwts.builder()
                .setSubject(email)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + RESET_EXP))
                .signWith(getKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractEmail(String token) {

        return parse(token).getBody().getSubject();
    }

    public String extractEmailFromResetToken(String token) {
        return extractEmail(token);
    }


    public boolean isValid(String token, User user) {

        String email = extractEmail(token);

        return email.equals(user.getEmail()) && !isExpired(token);
    }

    private boolean isExpired(String token) {

        Date exp = parse(token).getBody().getExpiration();

        return exp.before(new Date());
    }

    private Jws<Claims> parse(String token) {

        return Jwts.parserBuilder()
                .setSigningKey(getKey())
                .build()
                .parseClaimsJws(token);
    }
}