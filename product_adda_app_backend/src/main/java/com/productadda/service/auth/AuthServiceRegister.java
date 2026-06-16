package com.productadda.service.auth;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

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
import com.productadda.service.token.TokenService;
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
        private final TokenService.VerificationTokenService verificationTokenService;
        private final EmailVerificationTokenRepository emailVerificationTokenRepository;
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
                // Description: Validates incoming registration payload data fields.
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
                // 1.2 DATABASE LOOKUP VALIDATION
                // Description: Verifies uniqueness constraints against existing records.
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
                // Description: Determines which system authority role to assign.
                // ==========================================

                String requestedRole = request.getRoleName();
                String roleName;

                if (requestedRole == null || requestedRole.isBlank()) {
                        roleName = "USER";
                } else {
                        requestedRole = requestedRole.toUpperCase();

                        if (requestedRole.equals("ADMIN") || requestedRole.equals("SUPER_ADMIN")) {
                                throw new ApiException(
                                                HttpStatus.FORBIDDEN,
                                                "You are not allowed to assign ADMIN or SUPER_ADMIN roles");
                        }

                        roleName = requestedRole;
                }

                Role role = roleRepository.findByRoleName(roleName)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Invalid role: " + roleName));

                // ==========================================
                // 2.2 USER ENTITY CREATION
                // Description: Assembles user entity state and handles password encryption.
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
                                .build();

                // ==========================================
                // 2.3 EMAIL NOTIFICATION AND SECURITY ENGINE
                // Description: Coordinates verification tokens and external communication.
                // ==========================================

                emailVerificationTokenRepository.deleteByFkUser(userInstance);

                TokenService.VerificationTokenService.TokenResult tokenResult = verificationTokenService
                                .generateEmailVerificationToken();

                LocalDateTime expiresAtUtc = LocalDateTime.now(ZoneOffset.UTC)
                                .plus(verificationTokenExpiryMs, java.time.temporal.ChronoUnit.MILLIS);

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
                // Description: Persists core user account record.
                // ==========================================

                User user = userRepository.save(userInstance);

                // ==========================================
                // 3.2 USER ROLE SAVE
                // Description: Links account record to relational system role mapping.
                // ==========================================

                UserRole userRole = UserRole.builder()
                                .pkUserRoleId(uuidUtil.generateUuidV7())
                                .fkUser(user)
                                .fkRole(role)
                                .build();

                userRoleRepository.save(userRole);

                // ==========================================
                // 3.3 SECURITY SECURITY DATA SAVE
                // Description: Persists hashed verification tokens linked to the user account.
                // ==========================================

                EmailVerificationToken emailTokenEntity = EmailVerificationToken.builder()
                                .pkVerificationTokenId(uuidUtil.generateUuidV7())
                                .fkUser(user)
                                .verificationToken(tokenResult.hashedToken())
                                .isUsed(false)
                                .expiresAtUtc(expiresAtUtc)
                                .build();

                emailVerificationTokenRepository.save(emailTokenEntity);

                // ==========================================
                // 4. EMAIL NOTIFICATION
                // Description: Coordinates verification external communication.
                // ==========================================

                emailService.sendEmail(sendEmailRequest);

                /*
                 * ================================================================
                 * 5. RESPONSE SECTION
                 * Description: Maps persisted database state details into the final
                 * contract payload structure.
                 * ================================================================
                 */

                // ==========================================
                // 5.1 RESPONSE MAPPING
                // Description: Compiles output response representation wrapper data objects.
                // ==========================================

                return RegisterResponseDto.builder()
                                .userId(user.getPkUserId())
                                .email(user.getEmail())
                                .roleName(role.getRoleName())
                                .emailVerificationRequired(true)
                                .message("Registration successful. Verification email sent.")
                                .build();
        }
}