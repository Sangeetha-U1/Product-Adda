package com.productadda.service.token;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import javax.crypto.SecretKey;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import com.productadda.util.UuidUtil;
import com.productadda.util.HashUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import lombok.RequiredArgsConstructor;

public class TokenProvider {

        /*
         * ============================================================================
         * 1. JWT SERVICE
         * Description: Handles cryptographic calculations, token construction, and
         * claims verification.
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
                        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
                }

                // Multi-claim method baking trusty DB metadata directly into the JWT
                // token
                public String generateAccessToken(UUID userId, String email, List<String> roles) {
                        long nowMs = System.currentTimeMillis();
                        return Jwts.builder()
                                        .subject(email)
                                        .claim("userId", userId.toString())
                                        .claim("roles", roles)
                                        .issuedAt(new Date(nowMs))
                                        .expiration(new Date(nowMs + accessTokenExpiryMs))
                                        .signWith(getSigningKey())
                                        .compact();
                }

                public String generateRefreshToken(String email) {
                        long nowMs = System.currentTimeMillis();
                        return Jwts.builder()
                                        .subject(email)
                                        .issuedAt(new Date(nowMs))
                                        .expiration(new Date(nowMs + refreshTokenExpiryMs))
                                        .signWith(getSigningKey())
                                        .compact();
                }

                // ==========================================
                // EXTRACTION UTILITIES
                // ==========================================

                public String extractEmailFromToken(String token) {
                        return extractAllClaims(token).getSubject();
                }

                public UUID extractUserIdFromToken(String token) {
                        String userIdStr = extractAllClaims(token).get("userId", String.class);
                        return userIdStr != null ? UUID.fromString(userIdStr) : null;
                }

                @SuppressWarnings("unchecked")
                public List<String> extractRolesFromToken(String token) {
                        return extractAllClaims(token).get("roles", List.class);
                }

                // ==========================================
                // VALIDATION & PARSING
                // ==========================================

                public boolean isTokenExpired(String token) {
                        try {
                                return extractAllClaims(token)
                                                .getExpiration()
                                                .before(new Date());
                        } catch (Exception e) {
                                return true;
                        }
                }

                public boolean isTokenValid(String token) {
                        try {
                                return !isTokenExpired(token);
                        } catch (Exception exception) {
                                return false;
                        }
                }

                private Claims extractAllClaims(String token) {
                        return Jwts.parser()
                                        .verifyWith(getSigningKey())
                                        .build()
                                        .parseSignedClaims(token)
                                        .getPayload();
                }

                public LocalDateTime getRefreshTokenExpiryDate() {
                        return LocalDateTime.now(ZoneOffset.UTC)
                                        .plusSeconds(refreshTokenExpiryMs / 1000);
                }
        }

        /*
         * ============================================================================
         * 2. TOKEN SERVICE
         * Description: Generates, packages, and hashes opaque database verification
         * tokens.
         * ============================================================================
         */
        @Service
        @RequiredArgsConstructor
        public static class TokenService {

                private final UuidUtil uuidUtil;
                private final HashUtil hashUtil;

                public record TokenResult(String rawToken, String hashedToken) {
                }

                /**
                 * Generates a raw UUIDv7 token, hashes it, and returns both values.
                 */
                public TokenResult generateToken() {
                        String rawToken = uuidUtil.generateUuidV7String();
                        String hashedToken = hashToken(rawToken);

                        return new TokenResult(rawToken, hashedToken);
                }

                public String hashToken(String rawToken) {
                        if (rawToken == null || rawToken.isBlank()) {
                                return "";
                        }
                        return hashUtil.hashSha256(rawToken);
                }
        }
}