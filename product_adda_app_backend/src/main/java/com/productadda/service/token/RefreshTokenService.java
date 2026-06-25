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
                // CHANGED: Use the opaque token service instead of generating a
                // cryptographically signed JWT string
                TokenProvider.TokenService.TokenResult tokenResult = tokenService.generateToken();
                String rawRefreshToken = tokenResult.rawToken();
                String hashedRefreshToken = tokenResult.hashedToken();

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
                                .createdAtUtc(nowUtc)
                                .updatedAtUtc(nowUtc)
                                .isActive(true)
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
                                        "Refresh token value is missing or empty in the request payload");
                }

                // ==========================================
                // 1.2 SIGNATURE VALIDATION
                // ==========================================
                // REMOVED: jwtService.isTokenValid() validation block has been omitted here
                // because opaque tokens are random database strings, not cryptographic JWT
                // tokens.

                // ==========================================
                // 1.3 DATABASE RECORD LOOKUP
                // ==========================================
                String tokenHash = tokenService.hashToken(rawRefreshToken);

                RefreshToken refreshToken = refreshTokenRepository
                                .findByTokenHash(tokenHash)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.UNAUTHORIZED,
                                                "No active session found matching this refresh token hash. The token may not be stored in the database."));

                // ==========================================
                // 1.4 REVOCATION STATUS VALIDATION
                // ==========================================
                if (refreshToken.getRevokedAtUtc() != null) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Refresh token is invalid because it was explicitly revoked at: "
                                                        + refreshToken.getRevokedAtUtc());
                }

                if (!Boolean.TRUE.equals(refreshToken.getIsActive())) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Refresh token is invalid because its operational status is set to inactive");
                }

                // ==========================================
                // 1.5 TEMPORAL EXPIRATION VALIDATION
                // ==========================================
                if (refreshToken.getExpiresAtUtc().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                        throw new ApiException(
                                        HttpStatus.UNAUTHORIZED,
                                        "Refresh token session has expired temporally at: "
                                                        + refreshToken.getExpiresAtUtc() + " UTC");
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
                refreshToken.setUpdatedAtUtc(nowUtc);
                refreshToken.setIsActive(false);

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

        /*
         * ================================================================
         * EVICT ALL ACTIVE USER SESSIONS
         * Description: Systematically terminates all active sessions for a target user.
         * ================================================================
         */
        @Transactional
        public void evictAllUserSessions(User user) {
                if (user == null) {
                        return;
                }

                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                // Modifying any active records matching this entity boundary
                refreshTokenRepository.findAllByFkUserAndIsActiveTrue(user)
                                .forEach(token -> {
                                        token.setRevokedAtUtc(nowUtc);
                                        token.setUpdatedAtUtc(nowUtc);
                                        token.setIsActive(false);
                                        refreshTokenRepository.save(token);
                                });
        }
}