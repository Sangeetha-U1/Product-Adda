package com.productadda.service.token;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;

import org.springframework.stereotype.Service;

import com.productadda.util.UuidUtil;
import com.productadda.util.HashUtil;

import org.springframework.beans.factory.annotation.Value;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Service
public class TokenService {

    /*
     * ============================================================================
     * 1. JWT SERVICE
     * ============================================================================
     */
    @Service
    public static class JwtService {

        @Value("${app.jwt.secret-key}")
        private String jwtSecret;

        @Value("${app.jwt.access-token-expiration-ms}")
        private long accessTokenExpiryMs;

        @Value("${app.jwt.refresh-token-expiration-ms}")
        private long refreshTokenExpiryMs;

        public String generateAccessToken(String email) {
            SecretKey secretKey = Keys.hmacShaKeyFor(
                    jwtSecret.getBytes(StandardCharsets.UTF_8));

            return Jwts.builder()
                    .subject(email)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
                    .signWith(secretKey)
                    .compact();
        }

        public String generateRefreshToken(String email) {
            SecretKey secretKey = Keys.hmacShaKeyFor(
                    jwtSecret.getBytes(StandardCharsets.UTF_8));

            return Jwts.builder()
                    .subject(email)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiryMs))
                    .signWith(secretKey)
                    .compact();
        }

        public LocalDateTime getRefreshTokenExpiryDate() {
            return LocalDateTime.now()
                    .plusSeconds(refreshTokenExpiryMs / 1000);
        }
    }

    /*
     * ============================================================================
     * 2. VERIFICATION TOKEN SERVICE
     * ============================================================================
     */
    @Service
    @RequiredArgsConstructor
    public static class VerificationTokenService {

        private final UuidUtil uuidUtil;
        private final HashUtil hashUtil;

        // Simple record container to hold both values cleanly
        public record TokenResult(String rawToken, String hashedToken) {
        }

        /**
         * Generates a raw UUIDv7 token, hashes it, and returns both values.
         * Requires zero arguments.
         */
        public TokenResult generateEmailVerificationToken() {
            String rawToken = uuidUtil.generateUuidV7String();
            String hashedToken = hashUtil.hashSha256(rawToken);

            return new TokenResult(rawToken, hashedToken);
        }
    }
}