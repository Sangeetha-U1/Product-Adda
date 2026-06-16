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
import com.productadda.util.HashUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

        private final EmailVerificationTokenRepository emailVerificationTokenRepository;
        private final UserRepository userRepository;
        private final HashUtil hashUtil;

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Validates input requests and verifies system token values.
         * ================================================================
         */
        @Transactional
        public VerifyEmailResponseDto verifyEmail(VerifyEmailRequestDto request) {

                String rawToken = request.getToken();

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // Description: Checks if the incoming raw token is present and structured
                // correctly.
                // ==========================================

                if (rawToken == null || rawToken.trim().isEmpty()) {

                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "Verification token is required");
                }

                // ==========================================
                // 1.2 DATABASE LOOKUP VALIDATION
                // Description: Hashes the raw token and checks its existence in the database.
                // ==========================================

                String hashedToken = hashUtil.hashSha256(rawToken);

                EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository
                                .findByVerificationToken(hashedToken)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Invalid verification token"));

                if (Boolean.TRUE.equals(emailVerificationToken.getIsUsed())) {

                        throw new ApiException(
                                        HttpStatus.CONFLICT,
                                        "Email already verified");

                }

                if (emailVerificationToken.getExpiresAtUtc()
                                .isBefore(LocalDateTime.now(ZoneOffset.UTC))) {

                        throw new ApiException(
                                        HttpStatus.GONE,
                                        "Verification token expired");

                }

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * Description: Core execution flow managing status modifications and domain
                 * transitions.
                 * ================================================================
                 */

                User user = emailVerificationToken.getFkUser();

                user.setEmailVerified(true);
                user.setIsActive(true);

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Commits user alterations and token invalidation data to
                 * persistent storage.
                 * ================================================================
                 */

                userRepository.save(user);

                emailVerificationToken.setIsUsed(true);

                emailVerificationTokenRepository.save(emailVerificationToken);

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * Description: Completes processing and controls termination mapping.
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