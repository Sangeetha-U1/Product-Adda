package com.productadda.service.token;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.SendEmailRequestDto;
import com.productadda.dto.token.ResendVerificationEmailRequestDto;
import com.productadda.dto.token.ResendVerificationEmailResponseDto;
import com.productadda.entity.EmailVerificationToken;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.EmailVerificationTokenRepository;
import com.productadda.repository.UserRepository;
import com.productadda.service.mail.EmailService;
import com.productadda.service.token.TokenService.VerificationTokenService;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResendVerificationEmailService {

    private final UserRepository userRepository;

    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    private final VerificationTokenService verificationTokenService;

    private final EmailService emailService;

    private final UuidUtil uuidUtil;

    /*
     * =============================================================================
     * FRONTEND APPLICATION URL CONFIGURATION
     * =============================================================================
     */

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend.verify-email-path}")
    private String verifyEmailPath;

    @Value("${app.jwt.verification-token-expiration-ms}")
    private long verificationTokenExpiryMs;

    /*
     * ================================================================
     * 1. VALIDATION SECTION
     * Description: Validates resend request and verifies user eligibility.
     * ================================================================
     */

    @Transactional
    public ResendVerificationEmailResponseDto resend(
            ResendVerificationEmailRequestDto request) {

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // Description: Validates incoming email request.
        // ==========================================

        if (request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Email is required");
        }

        // ==========================================
        // 1.2 DATABASE LOOKUP VALIDATION
        // Description: Finds user and validates verification state.
        // ==========================================

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found"));

        if (Boolean.TRUE.equals(user.getEmailVerified())) {

            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "Email already verified");
        }

        /*
         * ================================================================
         * 2. BUSINESS SECTION
         * Description: Generates replacement verification token and email payload.
         * ================================================================
         */

        emailVerificationTokenRepository
                .deleteByFkUser(user);

        VerificationTokenService.TokenResult tokenResult = verificationTokenService
                .generateEmailVerificationToken();

        LocalDateTime expiresAtUtc = LocalDateTime.now(ZoneOffset.UTC)
                .plus(
                        verificationTokenExpiryMs,
                        ChronoUnit.MILLIS);

        String verificationUrl = frontendBaseUrl
                + verifyEmailPath
                + "?token="
                + tokenResult.rawToken();

        SendEmailRequestDto sendEmailRequest = SendEmailRequestDto.builder()
                .toEmail(user.getEmail())
                .subject("Verify your email")
                .body("""
                        You requested a new verification email for your Product Adda account.

                        Please verify your email by clicking below link:

                        %s

                        If you did not request this, please ignore this email.
                        """.formatted(verificationUrl))
                .build();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Description: Saves regenerated verification token.
         * ================================================================
         */

        EmailVerificationToken emailTokenEntity = EmailVerificationToken.builder()
                .pkVerificationTokenId(
                        uuidUtil.generateUuidV7())
                .fkUser(user)
                .verificationToken(
                        tokenResult.hashedToken())
                .isUsed(false)
                .expiresAtUtc(expiresAtUtc)
                .build();

        emailVerificationTokenRepository
                .save(emailTokenEntity);

        /*
         * ================================================================
         * 4. EMAIL NOTIFICATION SECTION
         * Description: Sends regenerated verification email.
         * ================================================================
         */

        emailService.sendEmail(sendEmailRequest);

        /*
         * ================================================================
         * 5. RESPONSE SECTION
         * Description: Returns resend verification response.
         * ================================================================
         */

        return ResendVerificationEmailResponseDto.builder()
                .userId(user.getPkUserId())
                .email(user.getEmail())
                .emailVerificationRequired(true)
                .message("Verification email sent successfully.")
                .build();
    }

}