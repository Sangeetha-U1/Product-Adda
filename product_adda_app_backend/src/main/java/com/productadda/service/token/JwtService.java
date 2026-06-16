package com.productadda.service.token;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

        /*
         * ================================================================
         * JWT CONFIGURATION
         * Description: Centralized JWT token generation settings.
         * ================================================================
         */

        @Value("${app.jwt.secret-key}")
        private String jwtSecret;

        @Value("${app.jwt.access-token-expiration-ms}")
        private long accessTokenExpiryMs;

        @Value("${app.jwt.refresh-token-expiration-ms}")
        private long refreshTokenExpiryMs;

        /*
         * ================================================================
         * 1. BUSINESS SECTION
         * Description: Generates JWT access token.
         * ================================================================
         */
        public String generateAccessToken(String email) {

                SecretKey secretKey = Keys.hmacShaKeyFor(
                                jwtSecret.getBytes(StandardCharsets.UTF_8));

                return Jwts.builder()
                                .subject(email)
                                .issuedAt(new Date())
                                .expiration(new Date(
                                                System.currentTimeMillis() + accessTokenExpiryMs))
                                .signWith(secretKey)
                                .compact();
        }

        /*
         * ================================================================
         * 2. BUSINESS SECTION
         * Description: Generates JWT refresh token.
         * ================================================================
         */
        public String generateRefreshToken(String email) {

                SecretKey secretKey = Keys.hmacShaKeyFor(
                                jwtSecret.getBytes(StandardCharsets.UTF_8));

                return Jwts.builder()
                                .subject(email)
                                .issuedAt(new Date())
                                .expiration(new Date(
                                                System.currentTimeMillis() + refreshTokenExpiryMs))
                                .signWith(secretKey)
                                .compact();
        }

        public LocalDateTime getRefreshTokenExpiryDate() {

                return LocalDateTime.now()
                                .plusSeconds(
                                                refreshTokenExpiryMs / 1000);
        }

}
