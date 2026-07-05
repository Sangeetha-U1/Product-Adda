package com.productadda.service.token;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import java.util.List;
import java.util.UUID;

import com.productadda.repository.RefreshTokenRepository;
import com.productadda.repository.UserRoleRepository;

import com.productadda.util.UuidUtil;
import com.productadda.exception.ApiException;

import com.productadda.entity.RefreshToken;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.dto.token.RefreshTokenRequestDto;
import com.productadda.dto.token.RefreshTokenResponseDto;
import com.productadda.dto.token.TokenDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

        private final RefreshTokenRepository refreshTokenRepository;
        private final UserRoleRepository userRoleRepository;

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
        public String createRefreshToken(User user, UUID tokenFamilyId) {

                if (user == null) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "User authentication failed or user does not exist");
                }

                TokenProvider.TokenService.TokenResult tokenResult = tokenService.generateToken();
                String rawRefreshToken = tokenResult.rawToken();
                String hashedRefreshToken = tokenResult.hashedToken();

                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                RefreshToken refreshToken = RefreshToken.builder()
                                .pkRefreshTokenId(uuidUtil.generateUuidV7())
                                .fkUser(user)
                                .tokenFamilyId(tokenFamilyId)
                                .tokenHash(hashedRefreshToken)
                                .expiresAtUtc(jwtService.getRefreshTokenExpiryDate())
                                .createdAtUtc(nowUtc)
                                .updatedAtUtc(nowUtc)
                                .isActive(true)
                                .revokedAtUtc(null)
                                .build();

                refreshTokenRepository.save(refreshToken);

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

                if (rawRefreshToken == null || rawRefreshToken.isBlank()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Refresh token value is missing or empty in the request payload");
                }

                /*
                 * ================================================================
                 * 2. DATABASE LOOKUP VALIDATION
                 * ================================================================
                 */

                String tokenHash = tokenService.hashToken(rawRefreshToken);

                RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                                                "No active session found matching this refresh token hash. The token may not be stored in the database."));

                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                /*
                 * ================================================================
                 * REUSE DETECTION (CRITICAL SECURITY CHECK)
                 * If token exists but is already revoked → replay attack
                 * ================================================================
                 */
                if (refreshToken.getRevokedAtUtc() != null) {

                        UUID compromisedFamilyId = refreshToken.getTokenFamilyId();

                        refreshTokenRepository.findAllByTokenFamilyIdAndIsActiveTrue(compromisedFamilyId)
                                        .forEach(token -> {
                                                token.setRevokedAtUtc(nowUtc);
                                                token.setIsActive(false);
                                        });

                        refreshTokenRepository.flush();

                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "Refresh token reuse detected. This session has been revoked. Please login again.");
                }

                /*
                 * ================================================================
                 * NORMAL VALIDATION
                 * ================================================================
                 */

                if (!Boolean.TRUE.equals(refreshToken.getIsActive())) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "Refresh token is inactive. Please login again.");
                }

                if (refreshToken.getExpiresAtUtc().isBefore(nowUtc)) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "Refresh token has expired at: " + refreshToken.getExpiresAtUtc() + " UTC");
                }

                /*
                 * ================================================================
                 * RESPONSE
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
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (refreshToken == null) {
                        return;
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                // Note: System-facing modification routine.

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                // No localized lookup evaluation required.

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
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

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Refresh token is missing in request payload");
                }

                String rawRefreshToken = request.getRefreshToken();

                /*
                 * ================================================================
                 * 2. VALIDATE TOKEN (FAIL FAST IF INVALID)
                 * ================================================================
                 */

                RefreshToken refreshToken = validateRefreshToken(rawRefreshToken);

                /*
                 * ================================================================
                 * 3. EXTRACT USER CONTEXT
                 * ================================================================
                 */

                User user = refreshToken.getFkUser();

                if (user == null) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "User context associated with this refresh token is missing");
                }

                List<UserRole> userRoles = userRoleRepository.findByFkUser(user);

                if (userRoles.isEmpty()) {
                        throw new ApiException(HttpStatus.NOT_FOUND,
                                        "User role mapping not found");
                }

                List<String> assignedRoles = userRoles.stream()
                                .map(userRole -> userRole.getFkRole().getRoleName())
                                .collect(Collectors.toList());

                /*
                 * ================================================================
                 * 4. SECURITY CHECK (TOKEN STATE VALIDATION ALREADY DONE)
                 * ================================================================
                 * At this point token is:
                 * - not expired
                 * - not revoked
                 * - active
                 * - not reused
                 */

                /*
                 * ================================================================
                 * 5. REVOKE OLD TOKEN (IMPORTANT: BEFORE ISSUING NEW ONE)
                 * ================================================================
                 */

                revokeRefreshToken(refreshToken);

                /*
                 * ================================================================
                 * 6. GENERATE NEW TOKENS
                 * ================================================================
                 */

                String accessToken = jwtService.generateAccessToken(
                                user.getPkUserId(),
                                user.getEmail(),
                                assignedRoles);

                UUID tokenFamilyId = uuidUtil.generateUuidV7();

                String newRefreshToken = createRefreshToken(user, tokenFamilyId);

                /*
                 * ================================================================
                 * 7. RESPONSE MAPPING
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

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                if (user == null) {
                        return;
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */

                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                List<RefreshToken> activeTokens = refreshTokenRepository.findAllByFkUserAndIsActiveTrue(user);

                if (activeTokens == null || activeTokens.isEmpty()) {
                        return;
                }

                /*
                 * ================================================================
                 * 3. STATE UPDATE (BATCH IN MEMORY)
                 * ================================================================
                 */

                activeTokens.forEach(token -> {
                        token.setRevokedAtUtc(nowUtc);
                        token.setUpdatedAtUtc(nowUtc);
                        token.setIsActive(false);
                });

                /*
                 * ================================================================
                 * 4. DB SAVE (ONE BATCH CALL)
                 * ================================================================
                 */

                refreshTokenRepository.saveAll(activeTokens);
        }
}