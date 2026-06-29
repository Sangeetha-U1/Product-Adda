package com.productadda.service.token;

import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.stream.Collectors;
import java.util.List;

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
        public String createRefreshToken(User user) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (user == null) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "User authentication failed or user does not exist");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                // Note: Security parameters inherited implicitly from the parameter signature
                // invocation.

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                // No supplemental lookup validations required.

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                TokenProvider.TokenService.TokenResult tokenResult = tokenService.generateToken();
                String rawRefreshToken = tokenResult.rawToken();
                String hashedRefreshToken = tokenResult.hashedToken();

                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

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

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                refreshTokenRepository.save(refreshToken);

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
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
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Refresh token value is missing or empty in the request payload");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                // Note: Open authorization validation routing checkpoint.

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                String tokenHash = tokenService.hashToken(rawRefreshToken);

                RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(tokenHash)
                                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED,
                                                "No active session found matching this refresh token hash. The token may not be stored in the database."));

                if (refreshToken.getRevokedAtUtc() != null) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "Refresh token is invalid because it was explicitly revoked at: "
                                                        + refreshToken.getRevokedAtUtc());
                }

                if (!Boolean.TRUE.equals(refreshToken.getIsActive())) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "Refresh token is invalid because its operational status is set to inactive");
                }

                if (refreshToken.getExpiresAtUtc().isBefore(LocalDateTime.now(ZoneOffset.UTC))) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "Refresh token session has expired temporally at: "
                                                        + refreshToken.getExpiresAtUtc() + " UTC");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                // Read-only structural tracking method. No business transitions applied.

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
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

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (request == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Rotation data body payload cannot be null");
                }

                String rawRefreshToken = request.getRefreshToken();

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                // Note: Opaque verification checkpoint. Validation handled down-funnel inside
                // token processing layer.

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                RefreshToken refreshToken = validateRefreshToken(rawRefreshToken);

                User user = refreshToken.getFkUser();
                if (user == null) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "User context associated with this session token is missing");
                }

                List<UserRole> userRoles = userRoleRepository.findByFkUser(user);
                if (userRoles.isEmpty()) {
                        throw new ApiException(HttpStatus.NOT_FOUND, "User role mapping not found");
                }

                List<String> assignedRoles = userRoles.stream()
                                .map(userRole -> userRole.getFkRole().getRoleName())
                                .collect(Collectors.toList());

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                String accessToken = jwtService.generateAccessToken(
                                user.getPkUserId(),
                                user.getEmail(),
                                assignedRoles);
                String newRefreshToken = createRefreshToken(user);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                revokeRefreshToken(refreshToken);

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
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

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (user == null) {
                        return;
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                // Note: Domain lifecycle hook.

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                // Dynamic collections evaluated dynamically on execution pipeline inside the
                // business step.

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                refreshTokenRepository.findAllByFkUserAndIsActiveTrue(user)
                                .forEach(token -> {
                                        token.setRevokedAtUtc(nowUtc);
                                        token.setUpdatedAtUtc(nowUtc);
                                        token.setIsActive(false);
                                        refreshTokenRepository.save(token);
                                });
        }
}