package com.productadda.service.auth;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.SendEmailRequestDto;
import com.productadda.dto.auth.RegisterRequestDto;
import com.productadda.dto.auth.RegisterResponseDto;
import com.productadda.entity.EmailVerificationToken;
import com.productadda.entity.Role;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.exception.ApiException;
import com.productadda.repository.EmailVerificationTokenRepository;
import com.productadda.repository.RoleRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.service.token.TokenProvider.TokenService;
import com.productadda.service.mail.EmailService;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceRegister {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final UserRoleRepository userRoleRepository;
        private final PasswordEncoder passwordEncoder;
        private final TokenService tokenService;
        private final EmailVerificationTokenRepository emailVerificationTokenRepository;
        private final EmailService emailService;
        private final UuidUtil uuidUtil;

        @Value("${app.frontend.base-url}")
        private String frontendBaseUrl;

        @Value("${app.frontend.verify-email-path}")
        private String verifyEmailPath;

        @Value("${app.jwt.verification-token-expiration-ms}")
        private long verificationTokenExpiryMs;

        @Transactional
        public RegisterResponseDto register(RegisterRequestDto request) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Validates incoming request data and verifies
                 * required database constraints before processing registration.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "First name is required");
                }

                if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Last name is required");
                }

                if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Email is required");
                }

                if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                // Note: Public registration endpoint. No security context criteria applies.

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Email already exists");
                }

                if (request.getMobile() != null
                                && !request.getMobile().trim().isEmpty()
                                && userRepository.existsByMobile(request.getMobile())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Mobile already exists");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * Description: Executes registration business rules, role resolution,
                 * password encryption, and verification workflow processing.
                 * ================================================================
                 */

                // ==========================================
                // 2.1 ROLE RESOLUTION
                // ==========================================
                String roleName = "USER";

                Role role = roleRepository.findByRoleName(roleName)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Invalid role: " + roleName));

                // Capture a unified timestamp record for all persistent row creations
                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                // ==========================================
                // 2.2 USER ENTITY CREATION
                // ==========================================
                User userInstance = User.builder()
                                .pkUserId(uuidUtil.generateUuidV7())
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .email(request.getEmail())
                                .mobile(request.getMobile())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                .emailVerified(false)
                                .isActive(false)
                                .createdAtUtc(nowUtc)
                                .updatedAtUtc(nowUtc)
                                .build();

                // ==========================================
                // 2.3 EMAIL NOTIFICATION AND SECURITY ENGINE
                // ==========================================
                TokenService.TokenResult tokenResult = tokenService.generateToken();

                LocalDateTime expiresAtUtc = nowUtc.plus(verificationTokenExpiryMs,
                                java.time.temporal.ChronoUnit.MILLIS);

                String verificationUrl = frontendBaseUrl
                                + verifyEmailPath
                                + "?token="
                                + tokenResult.rawToken();

                SendEmailRequestDto sendEmailRequest = SendEmailRequestDto.builder()
                                .toEmail(userInstance.getEmail())
                                .subject("Verify your email")
                                .body("""
                                                Welcome to Product Adda.

                                                Please verify your email by clicking below link:

                                                %s

                                                If you did not create this account, please ignore this email.
                                                """.formatted(verificationUrl))
                                .build();

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Persists primary user, structural role relationships,
                 * and generated verification data securely.
                 * ================================================================
                 */

                // ==========================================
                // 3.1 USER SAVE
                // ==========================================
                User user = userRepository.save(userInstance);

                // Delete operations executed *after* user has a formal persistent database
                // tracking state
                emailVerificationTokenRepository.deleteByFkUser(user);

                // ==========================================
                // 3.2 USER ROLE SAVE
                // ==========================================
                UserRole userRole = UserRole.builder()
                                .pkUserRoleId(uuidUtil.generateUuidV7())
                                .fkUser(user)
                                .fkRole(role)
                                .createdAtUtc(nowUtc)
                                .isActive(true)
                                .build();

                userRoleRepository.save(userRole);

                // ==========================================
                // 3.3 SECURITY DATA SAVE
                // ==========================================
                EmailVerificationToken emailTokenEntity = EmailVerificationToken.builder()
                                .pkVerificationTokenId(uuidUtil.generateUuidV7())
                                .fkUser(user)
                                .verificationToken(tokenResult.hashedToken())
                                .isUsed(false)
                                .isActive(true)
                                .createdAtUtc(nowUtc)
                                .expiresAtUtc(expiresAtUtc)
                                .build();

                emailVerificationTokenRepository.save(emailTokenEntity);

                // ==========================================
                // 4. EMAIL NOTIFICATION
                // ==========================================
                emailService.sendEmail(sendEmailRequest);

                /*
                 * ================================================================
                 * 5. RESPONSE SECTION
                 * ================================================================
                 */

                // ==========================================
                // 5.1 RESPONSE MAPPING
                // ==========================================
                String assignedRoleName = role.getRoleName();

                return RegisterResponseDto.builder()
                                .userId(user.getPkUserId())
                                .email(user.getEmail())
                                .roleName(assignedRoleName)
                                .emailVerificationRequired(true)
                                .message("Registration successful. Verification email sent.")
                                .build();
        }
}