package com.productadda.service.token;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;

import com.productadda.repository.RefreshTokenRepository;
import com.productadda.util.UuidUtil;
import com.productadda.entity.RefreshToken;
import com.productadda.exception.ApiException;
import com.productadda.entity.User;
import com.productadda.dto.token.RefreshTokenRequestDto;
import com.productadda.dto.token.RefreshTokenResponseDto;
import com.productadda.dto.token.TokenDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

        /*
         * ================================================================
         * REPOSITORIES
         * ================================================================
         */
        private final RefreshTokenRepository refreshTokenRepository;

        /*
         * ================================================================
         * SERVICES
         * ================================================================
         */
        private final TokenProvider.JwtService jwtService;
        private final TokenProvider.TokenService tokenService;

        /*
         * ================================================================
         * UTILITIES
         * ================================================================
         */
        private final UuidUtil uuidUtil;

        /*
         * ================================================================
         * CREATE REFRESH TOKEN
         * ================================================================
         */
        public String createRefreshToken(User user) {

                /*
                 * ============================================================
                 * 1. VALIDATION SECTION
                 * ============================================================
                 */
                if (user == null) {
                        throw new IllegalArgumentException("User cannot be null");
                }

                /*
                 * ============================================================
                 * 2. BUSINESS SECTION
                 * ============================================================
                 */
                String rawRefreshToken = jwtService.generateRefreshToken(
                                user.getEmail());

                String hashedRefreshToken = tokenService.hashToken(
                                rawRefreshToken);

                /*
                 * ============================================================
                 * 3. DB SAVING SECTION
                 * ============================================================
                 */
                RefreshToken refreshToken = RefreshToken.builder()
                                .pkRefreshTokenId(
                                                uuidUtil.generateUuidV7())
                                .fkUser(user)
                                .tokenHash(hashedRefreshToken)
                                .expiresAtUtc(
                                                jwtService.getRefreshTokenExpiryDate())
                                .revokedAtUtc(null)
                                .build();

                refreshTokenRepository.save(refreshToken);

                /*
                 * ============================================================
                 * 4. RESPONSE SECTION
                 * ============================================================
                 */
                return rawRefreshToken;
        }

        /*
         * ================================================================
         * VALIDATE REFRESH TOKEN
         * ================================================================
         */
        public RefreshToken validateRefreshToken(
                        String rawRefreshToken) {

                /*
                 * ============================================================
                 * 1. VALIDATION SECTION
                 * ============================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (rawRefreshToken == null
                                || rawRefreshToken.isBlank()) {

                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "Refresh token is required");
                }

                // ==========================================
                // 1.2 REFRESH VALIDATION
                // ==========================================
                if (!jwtService.isTokenValid(rawRefreshToken)) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid refresh token");
                }

                // ==========================================
                // 1.3 HASH TOKEN
                // ==========================================
                String tokenHash = tokenService.hashToken(rawRefreshToken);

                // ==========================================
                // 1.4 DATABASE LOOKUP
                // ==========================================
                RefreshToken refreshToken = refreshTokenRepository
                                .findByTokenHash(tokenHash)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Invalid refresh token"));

                // ==========================================
                // 1.5 REVOKED VALIDATION
                // ==========================================
                if (refreshToken.getRevokedAtUtc() != null) {

                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Refresh token has been revoked");
                }

                // ==========================================
                // 1.6 EXPIRY VALIDATION
                // ==========================================
                if (refreshToken.getExpiresAtUtc()
                                .isBefore(LocalDateTime.now())) {

                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Refresh token has expired");
                }

                /*
                 * ============================================================
                 * 2. RESPONSE SECTION
                 * ============================================================
                 */
                return refreshToken;
        }

        /*
         * ================================================================
         * REVOKE REFRESH TOKEN
         * ================================================================
         */
        public void revokeRefreshToken(
                        RefreshToken refreshToken) {

                refreshToken.setRevokedAtUtc(
                                LocalDateTime.now());

                refreshTokenRepository.save(
                                refreshToken);
        }

        /*
         * ================================================================
         * ROTATE REFRESH TOKEN
         * ================================================================
         */
        public RefreshTokenResponseDto rotateRefreshToken(
                        RefreshTokenRequestDto request) {

                String rawRefreshToken = request.getRefreshToken();

                /*
                 * ============================================================
                 * 1. VALIDATION SECTION
                 * ============================================================
                 */

                // ==========================================
                // 1.1 VALIDATE REFRESH TOKEN
                // ==========================================
                RefreshToken refreshToken = validateRefreshToken(
                                rawRefreshToken);

                /*
                 * ============================================================
                 * 2. BUSINESS SECTION
                 * ============================================================
                 */

                User user = refreshToken.getFkUser();

                String accessToken = jwtService.generateAccessToken(
                                user.getEmail());

                String newRefreshToken = createRefreshToken(user);

                /*
                 * ============================================================
                 * 3. DB SAVING SECTION
                 * ============================================================
                 */

                revokeRefreshToken(refreshToken);

                /*
                 * ============================================================
                 * 4. RESPONSE SECTION
                 * ============================================================
                 */

                TokenDto token = TokenDto.builder()
                                .accessToken(accessToken)
                                .refreshToken(newRefreshToken)
                                .build();

                return RefreshTokenResponseDto.builder()
                                .token(token)
                                .build();
        }
}