package com.productadda.service.token;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.token.ResetPasswordRequestDto;
import com.productadda.dto.token.ResetPasswordResponseDto;
import com.productadda.entity.PasswordResetToken;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.PasswordResetTokenRepository;
import com.productadda.repository.RefreshTokenRepository;
import com.productadda.repository.UserRepository;
import com.productadda.service.token.TokenProvider.TokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PasswordResetService {

        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final UserRepository userRepository;
        private final RefreshTokenRepository refreshTokenRepository;
        private final TokenService tokenService;
        private final PasswordEncoder passwordEncoder;

        @Value("${app.frontend.base-url}")
        private String frontendBaseUrl;

        @Value("${app.frontend.password-reset-email-path}")
        private String passwordResetEmailPath;

        @Value("${app.jwt.password-reset-token-expiration-ms}")
        private long passwordResetTokenExpiryMs;

        /*
         * ================================================================
         * RESET PASSWORD
         * Description: Validates the securely hashed token and overwrites credentials.
         * ================================================================
         */
        @Transactional
        public ResetPasswordResponseDto resetPassword(ResetPasswordRequestDto request) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (request == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Request body cannot be null");
                }

                if (request.getToken() == null || request.getToken().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Reset token is required");
                }

                if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "New password is required");
                }

                String tokenHash = tokenService.hashToken(request.getToken());

                // ==========================================
                // 1.2 DATABASE LOOKUP VALIDATION
                // ==========================================
                PasswordResetToken resetToken = passwordResetTokenRepository
                                .findByTokenHash(tokenHash)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.UNAUTHORIZED,
                                                "Invalid reset token"));

                if (Boolean.TRUE.equals(resetToken.getIsUsed()) || !Boolean.TRUE.equals(resetToken.getIsActive())) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Reset token already used or deactivated");
                }

                // Establish a single atomic point in time for validation and tracking
                // modifications
                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                if (resetToken.getExpiresAtUtc().isBefore(nowUtc)) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Reset token expired");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * ================================================================
                 */
                User user = resetToken.getFkUser();

                // Structural relationship safety guard
                if (user == null) {
                        throw new ApiException(HttpStatus.NOT_FOUND,
                                        "User associated with this token no longer exists");
                }

                // Encrypt password and record modification audit trail
                user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
                user.setUpdatedAtUtc(nowUtc);

                // Neutralize the token immediately to prevent replay attacks
                resetToken.setIsUsed(true);
                resetToken.setIsActive(false);
                resetToken.setUpdatedAtUtc(nowUtc);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                userRepository.save(user);
                passwordResetTokenRepository.save(resetToken);

                // Important Security Measure: Drop all current active logged-in sessions
                // everywhere
                refreshTokenRepository.deleteByFkUser(user);

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * ================================================================
                 */
                String userEmail = user.getEmail();

                return ResetPasswordResponseDto.builder()
                                .email(userEmail)
                                .build();
        }
}