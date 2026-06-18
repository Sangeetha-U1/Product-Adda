package com.productadda.service.token;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import com.productadda.dto.SendEmailRequestDto;
import com.productadda.dto.token.ResendVerificationEmailRequestDto;
import com.productadda.dto.token.ResendVerificationEmailResponseDto;
import com.productadda.entity.EmailVerificationToken;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.EmailVerificationTokenRepository;
import com.productadda.repository.UserRepository;
import com.productadda.service.mail.EmailService;
import com.productadda.service.token.TokenProvider.TokenService;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ResendVerificationEmailService {

        private final UserRepository userRepository;
        private final EmailVerificationTokenRepository emailVerificationTokenRepository;
        private final TokenService verificationTokenService;
        private final EmailService emailService;
        private final UuidUtil uuidUtil;
        private final TransactionTemplate transactionTemplate;

        @Value("${app.frontend.base-url}")
        private String frontendBaseUrl;

        @Value("${app.frontend.verify-email-path}")
        private String verifyEmailPath;

        @Value("${app.jwt.verification-token-expiration-ms}")
        private long verificationTokenExpiryMs;

        /*
         * ================================================================
         * RESEND VERIFICATION EMAIL
         * Description: Manages single transaction commits for tokens and pushes
         * external mail notifications out safely post-commit.
         * ================================================================
         */
        public ResendVerificationEmailResponseDto resend(ResendVerificationEmailRequestDto request) {

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Email is required");
                }

                // ==========================================
                // 1.2 DATABASE LOOKUP VALIDATION
                // ==========================================
                User user = userRepository.findByEmail(request.getEmail())
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

                if (Boolean.TRUE.equals(user.getEmailVerified())) {
                        throw new ApiException(HttpStatus.CONFLICT, "Email already verified");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION & DB TRANSACTION
                 * Description: Synchronizes state transitions atomically inside an execution
                 * block.
                 * ================================================================
                 */
                TokenService.TokenResult tokenResult = verificationTokenService.generateToken();
                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
                LocalDateTime expiresAtUtc = nowUtc.plus(verificationTokenExpiryMs, ChronoUnit.MILLIS);

                // Use transactionTemplate to ensure database operations complete and close
                // connections cleanly
                transactionTemplate.executeWithoutResult(status -> {
                        // Evict any old, unconsumed verification tokens
                        emailVerificationTokenRepository.deleteByFkUser(user);

                        // Synchronize audit records on the parent user container
                        user.setUpdatedAtUtc(nowUtc);
                        userRepository.save(user);

                        // Map the fresh token model with all explicit truth attributes
                        EmailVerificationToken emailTokenEntity = EmailVerificationToken.builder()
                                        .pkVerificationTokenId(uuidUtil.generateUuidV7())
                                        .fkUser(user)
                                        .verificationToken(tokenResult.hashedToken())
                                        .isUsed(false)
                                        .isActive(true) // Fixed missing property
                                        .createdAtUtc(nowUtc) // Fixed missing property
                                        .updatedAtUtc(nowUtc) // Fixed missing property
                                        .expiresAtUtc(expiresAtUtc)
                                        .build();

                        emailVerificationTokenRepository.save(emailTokenEntity);
                });

                /*
                 * ================================================================
                 * 3. EMAIL NOTIFICATION SECTION (Post-Commit Execution)
                 * Description: Dispatched safely outside DB locks to protect connection memory
                 * pools.
                 * ================================================================
                 */
                String verificationUrl = frontendBaseUrl + verifyEmailPath + "?token=" + tokenResult.rawToken();

                SendEmailRequestDto sendEmailRequest = SendEmailRequestDto.builder()
                                .toEmail(user.getEmail())
                                .subject("Verify your email")
                                .body("""
                                                You requested a new verification email for your Product Adda account.

                                                Please verify your email by clicking the link below:

                                                %s

                                                If you did not request this, please ignore this email.
                                                """.formatted(verificationUrl))
                                .build();

                try {
                        emailService.sendEmail(sendEmailRequest);
                } catch (Exception e) {
                        // Prevents standard mail server drops from rolling back structural database
                        // truth updates
                        throw new ApiException(
                                        HttpStatus.INTERNAL_SERVER_ERROR,
                                        "Token updated, but system failed to transmit email notice. Please retry.");
                }

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
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