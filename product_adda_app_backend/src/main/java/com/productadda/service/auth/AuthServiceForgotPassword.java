package com.productadda.service.auth;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.SendEmailRequestDto;
import com.productadda.dto.token.ForgotPasswordRequestDto;
import com.productadda.dto.token.ForgotPasswordResponsetDto;
import com.productadda.entity.PasswordResetToken;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.PasswordResetTokenRepository;
import com.productadda.repository.UserRepository;
import com.productadda.service.mail.EmailService;
import com.productadda.service.token.TokenProvider.TokenService;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceForgotPassword {

        private final PasswordResetTokenRepository passwordResetTokenRepository;
        private final UserRepository userRepository;
        private final TokenService tokenService;
        private final EmailService emailService;
        private final UuidUtil uuidUtil;

        @Value("${app.frontend.base-url}")
        private String frontendBaseUrl;

        @Value("${app.frontend.password-reset-email-path}")
        private String passwordResetEmailPath;

        @Value("${app.jwt.password-reset-token-expiration-ms}")
        private long passwordResetTokenExpiryMs;

        @Transactional
        public ForgotPasswordResponsetDto forgotPassword(ForgotPasswordRequestDto request) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Validates password reset request and verifies user eligibility.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // Description: Validates incoming email request.
                // ==========================================

                if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Email must not be null or empty");
                }

                // ==========================================
                // 1.2 DATABASE LOOKUP VALIDATION
                // Description: Finds user and validates verification state.
                // ==========================================

                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * Description: Generates replacement verification token and email payload.
                 * ================================================================
                 */

                // TODO: Later, change to just mark it in active some sort not hard delete.
                passwordResetTokenRepository
                                .deleteByFkUser(user);

                TokenService.TokenResult tokenResult = tokenService.generateToken();

                String rawToken = tokenResult.rawToken();

                String tokenHash = tokenResult.hashedToken();

                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                LocalDateTime expiresAtUtc = nowUtc.plus(passwordResetTokenExpiryMs,
                                java.time.temporal.ChronoUnit.MILLIS);

                String resetPasswordUrl = frontendBaseUrl
                                + passwordResetEmailPath
                                + "?token="
                                + rawToken;

                SendEmailRequestDto sendEmailRequest = SendEmailRequestDto.builder()
                                .toEmail(user.getEmail())
                                .subject("Password Reset Request")
                                .body(
                                                """
                                                                Hello,

                                                                You requested a password reset.

                                                                Reset your password using this link:

                                                                %s

                                                                This link expires in 30 minutes.

                                                                If you did not request this,
                                                                ignore this email.
                                                                """
                                                                .formatted(resetPasswordUrl))
                                .build();

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Saves regenerated password reset token.
                 * ================================================================
                 */
                PasswordResetToken passwordResetToken = PasswordResetToken.builder()
                                .pkResetTokenId(
                                                uuidUtil.generateUuidV7())
                                .fkUser(user)
                                .tokenHash(tokenHash)
                                .createdAtUtc(nowUtc)
                                .expiresAtUtc(expiresAtUtc)
                                .isUsed(false)
                                .isActive(true)
                                .build();

                passwordResetTokenRepository.save(
                                passwordResetToken);

                /*
                 * ================================================================
                 * 4. EMAIL NOTIFICATION SECTION
                 * Description: Sends regenerated password reset email.
                 * ================================================================
                 */

                emailService.sendEmail(sendEmailRequest);

                /*
                 * ================================================================
                 * 5. RESPONSE SECTION
                 * Description: Returns resend password reset response.
                 * ================================================================
                 */
                return ForgotPasswordResponsetDto.builder()
                                .email(user.getEmail())
                                .build();
        }
}