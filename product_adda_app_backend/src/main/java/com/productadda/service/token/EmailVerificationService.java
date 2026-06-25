package com.productadda.service.token;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.token.VerifyEmailRequestDto;
import com.productadda.dto.token.VerifyEmailResponseDto;
import com.productadda.entity.EmailVerificationToken;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.EmailVerificationTokenRepository;
import com.productadda.repository.UserRepository;
import com.productadda.service.token.TokenProvider.TokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

        private final EmailVerificationTokenRepository emailVerificationTokenRepository;
        private final UserRepository userRepository;
        private final TokenService verificationTokenService;

        /*
         * ================================================================
         * EMAIL VERIFICATION SERVICE
         * Description: Processes raw validation tokens to update corresponding user
         * activation status.
         * ================================================================
         */
        @Transactional
        public VerifyEmailResponseDto verifyEmail(VerifyEmailRequestDto request) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (request == null || request.getToken() == null || request.getToken().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Verification token is required");
                }

                String rawToken = request.getToken();

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                // Note: Public unauthenticated verification entry point.

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                String hashedToken = verificationTokenService.hashToken(rawToken);

                EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository
                                .findByVerificationToken(hashedToken)
                                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                                                "Invalid verification token"));

                if (Boolean.TRUE.equals(emailVerificationToken.getIsUsed())
                                || !Boolean.TRUE.equals(emailVerificationToken.getIsActive())) {
                        throw new ApiException(HttpStatus.CONFLICT, "Email already verified or token has been revoked");
                }

                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                if (emailVerificationToken.getExpiresAtUtc().isBefore(nowUtc)) {
                        throw new ApiException(HttpStatus.GONE, "Verification token expired");
                }

                User user = emailVerificationToken.getFkUser();
                if (user == null) {
                        throw new ApiException(HttpStatus.NOT_FOUND,
                                        "The user account associated with this token no longer exists");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                user.setEmailVerified(true);
                user.setIsActive(true);
                user.setUpdatedAtUtc(nowUtc);

                emailVerificationToken.setIsUsed(true);
                emailVerificationToken.setIsActive(false);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                userRepository.save(user);
                emailVerificationTokenRepository.save(emailVerificationToken);

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
                 * ================================================================
                 */
                return VerifyEmailResponseDto.builder()
                                .userId(user.getPkUserId())
                                .email(user.getEmail())
                                .emailVerified(user.getEmailVerified())
                                .isActive(user.getIsActive())
                                .build();
        }
}