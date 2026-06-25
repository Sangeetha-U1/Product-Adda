package com.productadda.service.admin;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.SendEmailRequestDto;
import com.productadda.dto.admin.AdminUserCreateRequestDto;
import com.productadda.dto.admin.AdminUserCreateResponseDto;
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
public class AdminUserCreateService {

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
        public AdminUserCreateResponseDto createAdminUser(AdminUserCreateRequestDto request) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                String targetRoleName = request.getTargetRole().trim().toUpperCase();

                if (!"ADMIN".equals(targetRoleName) && !"SUPER_ADMIN".equals(targetRoleName)) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Invalid role parameter assignment: Only ADMIN or SUPER_ADMIN roles are permitted");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED,
                                        "Security context is missing raw authentication data");
                }

                boolean isSuperAdminActor = authentication.getAuthorities().stream()
                                .map(GrantedAuthority::getAuthority)
                                .anyMatch(role -> role.equals("ROLE_SUPER_ADMIN") || role.equals("SUPER_ADMIN"));

                if ("SUPER_ADMIN".equals(targetRoleName) && !isSuperAdminActor) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Access denied: Standard administrative credentials cannot escalate privileges to SUPER_ADMIN");
                }

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Email allocation conflict: Target email already exists");
                }

                if (request.getMobile() != null && !request.getMobile().trim().isEmpty()
                                && userRepository.existsByMobile(request.getMobile())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Mobile allocation conflict: Target mobile index already exists");
                }

                Role role = roleRepository.findByRoleName(targetRoleName)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Target security profile configuration mapping not found: "
                                                                + targetRoleName));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & DB PERSISTENCE
                 * ================================================================
                 */
                LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

                User adminUserInstance = User.builder()
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

                User savedAdminUser = userRepository.save(adminUserInstance);

                UserRole userRoleRelation = UserRole.builder()
                                .pkUserRoleId(uuidUtil.generateUuidV7())
                                .fkUser(savedAdminUser)
                                .fkRole(role)
                                .createdAtUtc(nowUtc)
                                .isActive(true)
                                .build();

                userRoleRepository.save(userRoleRelation);

                /*
                 * ================================================================
                 * 3. EMAIL VERIFICATION ENGINE WORKFLOW
                 * ================================================================
                 */
                TokenService.TokenResult tokenResult = tokenService.generateToken();

                LocalDateTime expiresAtUtc = nowUtc.plus(verificationTokenExpiryMs,
                                java.time.temporal.ChronoUnit.MILLIS);

                String verificationUrl = frontendBaseUrl
                                + verifyEmailPath
                                + "?token="
                                + tokenResult.rawToken();

                SendEmailRequestDto sendEmailRequest = SendEmailRequestDto.builder()
                                .toEmail(savedAdminUser.getEmail())
                                .subject("Verify your administrative account")
                                .body("""
                                                Welcome to Product Adda Management.

                                                An administrative profile has been provisioned for you.
                                                Please complete registration setup by verifying your email below:

                                                %s

                                                If you did not expect this invitation, please ignore this email.
                                                """.formatted(verificationUrl))
                                .build();

                EmailVerificationToken emailTokenEntity = EmailVerificationToken.builder()
                                .pkVerificationTokenId(uuidUtil.generateUuidV7())
                                .fkUser(savedAdminUser)
                                .verificationToken(tokenResult.hashedToken())
                                .isUsed(false)
                                .isActive(true)
                                .createdAtUtc(nowUtc)
                                .expiresAtUtc(expiresAtUtc)
                                .build();

                emailVerificationTokenRepository.save(emailTokenEntity);

                emailService.sendEmail(sendEmailRequest);

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
                 * ================================================================
                 */
                return AdminUserCreateResponseDto.builder()
                                .userId(savedAdminUser.getPkUserId())
                                .email(savedAdminUser.getEmail())
                                .assignedRole(role.getRoleName())
                                .isActive(savedAdminUser.getIsActive())
                                .build();
        }
}