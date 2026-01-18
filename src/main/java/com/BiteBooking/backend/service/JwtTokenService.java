package com.BiteBooking.backend.service;

import com.BiteBooking.backend.model.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;

@Service
public class JwtTokenService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private static final long EXPIRES_MINUTES = 120;

    public String generateToken(User user){

        Instant now = Instant.now();
        Instant expiry = now.plus(EXPIRES_MINUTES, ChronoUnit.MINUTES);

        byte[] key = Base64.getDecoder().decode(jwtSecret);

        return Jwts.builder()
                .subject(String.valueOf(user.getId()))
                .signWith(Keys.hmacShaKeyFor(key))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .claim("role", user.getRole())
                .claim("email", user.getEmail())
                .compact();
    }
}
