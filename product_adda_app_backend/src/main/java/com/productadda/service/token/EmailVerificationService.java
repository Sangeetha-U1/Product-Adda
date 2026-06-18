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
         * 1. VALIDATION SECTION
         * Description: Validates input requests and verifies system token values.
         * ================================================================
         */
        @Transactional
        public VerifyEmailResponseDto verifyEmail(VerifyEmailRequestDto request) {

                String rawToken = request.getToken();

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (rawToken == null || rawToken.trim().isEmpty()) {
                        throw new ApiException(
                                        HttpStatus.BAD_REQUEST,
                                        "Verification token is required");
                }

                // ==========================================
                // 1.2 DATABASE LOOKUP VALIDATION
                // ==========================================
                String hashedToken = verificationTokenService.hashToken(rawToken);

                EmailVerificationToken emailVerificationToken = emailVerificationTokenRepository
                                .findByVerificationToken(hashedToken)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Invalid verification token"));

                if (Boolean.TRUE.equals(emailVerificationToken.getIsUsed())
                                || !Boolean.TRUE.equals(emailVerificationToken.getIsActive())) {
                        throw new ApiException(
                                        HttpStatus.CONFLICT,
                                        "Email already verified or token has been revoked");
                }

                // Capture a single, consistent clock execution checkpoint for validations and
                // audits
                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                if (emailVerificationToken.getExpiresAtUtc().isBefore(nowUtc)) {
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

                // Guard check preventing relational null pointer faults if structural user
                // profiles are deleted
                if (user == null) {
                        throw new ApiException(
                                        HttpStatus.NOT_FOUND,
                                        "The user account associated with this token no longer exists");
                }

                user.setEmailVerified(true);
                user.setIsActive(true);
                user.setUpdatedAtUtc(nowUtc); // Added to sync database audit metadata state checks

                emailVerificationToken.setIsUsed(true);
                emailVerificationToken.setIsActive(false); // Added to ensure token is completely neutralized from
                                                           // search queries

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Commits user alterations and token invalidation data to
                 * persistent storage.
                 * ================================================================
                 */
                userRepository.save(user);
                emailVerificationTokenRepository.save(emailVerificationToken);

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
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