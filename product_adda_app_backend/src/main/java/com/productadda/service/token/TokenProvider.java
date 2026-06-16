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
public class TokenProvider {

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

        private SecretKey getSigningKey() {
            return Keys.hmacShaKeyFor(
                    jwtSecret.getBytes(StandardCharsets.UTF_8));
        }

        public String generateAccessToken(String email) {

            return Jwts.builder()
                    .subject(email)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + accessTokenExpiryMs))
                    .signWith(getSigningKey())
                    .compact();
        }

        public String generateRefreshToken(String email) {

            return Jwts.builder()
                    .subject(email)
                    .issuedAt(new Date())
                    .expiration(new Date(System.currentTimeMillis() + refreshTokenExpiryMs))
                    .signWith(getSigningKey())
                    .compact();
        }

        public String extractEmailFromToken(String token) {
            return extractAllClaims(token)
                    .getSubject();
        }

        public boolean isTokenExpired(String token) {
            return extractAllClaims(token)
                    .getExpiration()
                    .before(new Date());
        }

        public boolean isTokenValid(String token) {

            try {

                return !isTokenExpired(token);

            } catch (Exception exception) {

                return false;
            }
        }

        private io.jsonwebtoken.Claims extractAllClaims(String token) {

            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        }

        public LocalDateTime getRefreshTokenExpiryDate() {
            return LocalDateTime.now()
                    .plusSeconds(refreshTokenExpiryMs / 1000);
        }
    }

    /*
     * ============================================================================
     * 2. TOKEN SERVICE
     * ============================================================================
     */
    @Service
    @RequiredArgsConstructor
    public static class TokenService {

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

        public String hashToken(String rawToken) {
            String hashedToken = hashUtil.hashSha256(rawToken);

            return hashedToken;
        }
    }
}