package com.productadda.service.token;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

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

        private final RefreshTokenRepository refreshTokenRepository;
        private final TokenProvider.JwtService jwtService;
        private final TokenProvider.TokenService tokenService;
        private final UuidUtil uuidUtil;

        /*
         * ================================================================
         * CREATE REFRESH TOKEN
         * Description: Generates and persists a fresh refresh token lifecycle.
         * ================================================================
         */
        @Transactional
        public String createRefreshToken(User user) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */
                if (user == null) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User authentication failed or user does not exist");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * ================================================================
                 */
                String rawRefreshToken = jwtService.generateRefreshToken(user.getEmail());
                String hashedRefreshToken = tokenService.hashToken(rawRefreshToken);

                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                RefreshToken refreshToken = RefreshToken.builder()
                                .pkRefreshTokenId(uuidUtil.generateUuidV7())
                                .fkUser(user)
                                .tokenHash(hashedRefreshToken)
                                .expiresAtUtc(jwtService.getRefreshTokenExpiryDate())
                                .createdAtUtc(nowUtc) // Synchronized structural audit field
                                .updatedAtUtc(nowUtc) // Synchronized structural audit field
                                .isActive(true) // Live initialization flag
                                .revokedAtUtc(null)
                                .build();

                refreshTokenRepository.save(refreshToken);

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * ================================================================
                 */
                return rawRefreshToken;
        }

        /*
         * ================================================================
         * VALIDATE REFRESH TOKEN
         * Description: Processes runtime checks on arbitrary incoming strings.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public RefreshToken validateRefreshToken(String rawRefreshToken) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "Refresh token is required");
                }

                // ==========================================
                // 1.2 DATABASE LOOKUP VALIDATION
                // ==========================================
                if (!jwtService.isTokenValid(rawRefreshToken)) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Invalid refresh token signature");
                }

                String tokenHash = tokenService.hashToken(rawRefreshToken);

                RefreshToken refreshToken = refreshTokenRepository
                                .findByTokenHash(tokenHash)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Invalid refresh token"));

                if (refreshToken.getRevokedAtUtc() != null || !Boolean.TRUE.equals(refreshToken.getIsActive())) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Refresh token has been revoked");
                }

                // CRITICAL FIX: Forces time validation evaluation exclusively against UTC
                if (refreshToken.getExpiresAtUtc().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Refresh token has expired");
                }

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * ================================================================
                 */
                return refreshToken;
        }

        /*
         * ================================================================
         * REVOKE REFRESH TOKEN
         * Description: Inactivates authorization scopes by changing database states.
         * ================================================================
         */
        @Transactional
        public void revokeRefreshToken(RefreshToken refreshToken) {

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * ================================================================
                 */
                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                refreshToken.setRevokedAtUtc(nowUtc);
                refreshToken.setUpdatedAtUtc(nowUtc); // Track structural entity mutations
                refreshToken.setIsActive(false); // Drop token status from active scans

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                refreshTokenRepository.save(refreshToken);
        }

        /*
         * ================================================================
         * ROTATE REFRESH TOKEN
         * Description: Orchestrates atomic rotation transitions for active tokens.
         * ================================================================
         */
        @Transactional
        public RefreshTokenResponseDto rotateRefreshToken(RefreshTokenRequestDto request) {

                String rawRefreshToken = request.getRefreshToken();

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */
                RefreshToken refreshToken = validateRefreshToken(rawRefreshToken);

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * ================================================================
                 */
                User user = refreshToken.getFkUser();

                if (user == null) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "User context associated with this session token is missing");
                }

                String accessToken = jwtService.generateAccessToken(user.getEmail());
                String newRefreshToken = createRefreshToken(user);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                revokeRefreshToken(refreshToken);

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * ================================================================
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