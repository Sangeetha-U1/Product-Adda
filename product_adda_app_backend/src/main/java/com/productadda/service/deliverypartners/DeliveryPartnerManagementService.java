package com.productadda.service.deliverypartners;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.SendEmailRequestDto;
import com.productadda.dto.deliverypartner.DeliveryPartnerCreateRequestDto;
import com.productadda.dto.deliverypartner.DeliveryPartnerResponseDto;
import com.productadda.entity.DeliveryPartner;
import com.productadda.entity.DeliveryPartnerStatus;
import com.productadda.entity.EmailVerificationToken;
import com.productadda.entity.Role;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.DeliveryPartnerRepository;
import com.productadda.repository.DeliveryPartnerStatusRepository;
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
public class DeliveryPartnerManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final DeliveryPartnerRepository deliveryPartnerRepository;
    private final DeliveryPartnerStatusRepository deliveryPartnerStatusRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailService emailService;
    private final UuidUtil uuidUtil;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.frontend.verify-email-path}")
    private String verifyEmailPath;

    @Value("${app.jwt.verification-token-expiration-ms}")
    private long verificationTokenExpiryMs;

    /*
     * ================================================================
     * CREATE DELIVERY PARTNER
     * Description: Onboards a new delivery partner user account and profile.
     * Admin-only access. Generates registration token and confirmation email.
     * ================================================================
     */
    @Transactional
    public DeliveryPartnerResponseDto createDeliveryPartner(DeliveryPartnerCreateRequestDto request) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Request body is required");
        }
        if (request.getPartnerName() == null || request.getPartnerName().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "partnerName is required");
        }
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "firstName is required");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "lastName is required");
        }
        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "email is required");
        }
        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "password is required");
        }
        if (request.getPhone() == null || !request.getPhone().matches("^[0-9]{10}$")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "phone must be 10 digits");
        }
        if (request.getMaxConcurrentDeliveries() == null || request.getMaxConcurrentDeliveries() <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "maxConcurrentDeliveries must be greater than 0");
        }
        if (request.getMaxConcurrentDeliveries() > 1000) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "maxConcurrentDeliveries must not exceed 1000");
        }

        String safePartnerName = request.getPartnerName().trim();
        String safeEmail = request.getEmail().trim();
        String safePhone = request.getPhone().trim();
        String safeCurrentLocation = (request.getCurrentLocation() == null
                || request.getCurrentLocation().trim().isEmpty())
                        ? null
                        : request.getCurrentLocation().trim();

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION (ADMIN ONLY)
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user no longer exists"));

        List<UserRole> currentRoles = userRoleRepository.findByFkUser(currentUser);
        if (currentRoles.isEmpty()) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to create delivery partners");
        }

        boolean hasAdminPrivileges = currentRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .anyMatch(roleName -> "ADMIN".equals(roleName) || "SUPER_ADMIN".equals(roleName));

        if (!hasAdminPrivileges) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin role required to create delivery partners");
        }

        if (userRepository.existsByEmail(safeEmail)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email " + safeEmail + " is already registered");
        }

        if (userRepository.existsByMobile(safePhone)) {
            throw new ApiException(HttpStatus.CONFLICT, "Mobile number " + safePhone + " is already registered");
        }

        DeliveryPartnerStatus availableStatus = deliveryPartnerStatusRepository.findByStatusName("AVAILABLE")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "AVAILABLE delivery partner status lookup row not found"));

        String targetRoleName = "DELIVERY_PARTNER";
        Role targetRole = roleRepository.findByRoleName(targetRoleName)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Invalid system role: " + targetRoleName));

        /*
         * ================================================================
         * 2. BUSINESS SECTION & ENTITY BUILDING
         * ================================================================
         */
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        // 2.1 Build User Entry
        User userInstance = User.builder()
                .pkUserId(uuidUtil.generateUuidV7())
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(safeEmail)
                .mobile(safePhone)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .isActive(false)
                .createdAtUtc(nowUtc)
                .updatedAtUtc(nowUtc)
                .build();

        // 2.2 Build Verification Security Objects
        TokenService.TokenResult tokenResult = tokenService.generateToken();
        LocalDateTime expiresAtUtc = nowUtc.plus(verificationTokenExpiryMs, java.time.temporal.ChronoUnit.MILLIS);

        String verificationUrl = frontendBaseUrl + verifyEmailPath + "?token=" + tokenResult.rawToken();

        SendEmailRequestDto sendEmailRequest = SendEmailRequestDto.builder()
                .toEmail(userInstance.getEmail())
                .subject("Verify your Delivery Partner Account")
                .body("""
                        Welcome to Product Adda Delivery Ecosystem.

                        An administrator has registered your profile. Please complete your registration verification by clicking below:

                        %s

                        If this profile was not generated intentionally, please disregard this email.
                        """
                        .formatted(verificationUrl))
                .build();

        // 2.3 Build Delivery Partner Profile
        DeliveryPartner partnerInstance = DeliveryPartner.builder()
                .pkDeliveryPartnerId(uuidUtil.generateUuidV7())
                .fkUser(userInstance)
                .fkStatus(availableStatus)
                .partnerName(safePartnerName)
                .currentLocation(safeCurrentLocation)
                .maxConcurrentDeliveries(request.getMaxConcurrentDeliveries())
                .activeDeliveries(0)
                .rating(BigDecimal.ZERO)
                .totalDeliveries(0)
                .joinedAt(nowUtc)
                .isActive(true)
                .build();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        User savedUser = userRepository.save(userInstance);
        emailVerificationTokenRepository.deleteByFkUser(savedUser);

        UserRole mappingRoleRecord = UserRole.builder()
                .pkUserRoleId(uuidUtil.generateUuidV7())
                .fkUser(savedUser)
                .fkRole(targetRole)
                .createdAtUtc(nowUtc)
                .isActive(true)
                .build();
        userRoleRepository.save(mappingRoleRecord);

        EmailVerificationToken emailTokenEntity = EmailVerificationToken.builder()
                .pkVerificationTokenId(uuidUtil.generateUuidV7())
                .fkUser(savedUser)
                .verificationToken(tokenResult.hashedToken())
                .isUsed(false)
                .isActive(true)
                .createdAtUtc(nowUtc)
                .expiresAtUtc(expiresAtUtc)
                .build();
        emailVerificationTokenRepository.save(emailTokenEntity);

        DeliveryPartner savedPartner = deliveryPartnerRepository.save(partnerInstance);

        /*
         * ================================================================
         * 4. NOTIFICATION ENGINE
         * ================================================================
         */
        emailService.sendEmail(sendEmailRequest);

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return DeliveryPartnerResponseDto.builder()
                .partnerId(savedPartner.getPkDeliveryPartnerId())
                .partnerName(savedPartner.getPartnerName())
                .email(savedUser.getEmail())
                .phone(savedUser.getMobile())
                .currentLocation(savedPartner.getCurrentLocation())
                .statusName(savedPartner.getFkStatus().getStatusName())
                .rating(savedPartner.getRating())
                .totalDeliveries(savedPartner.getTotalDeliveries())
                .activeDeliveries(savedPartner.getActiveDeliveries())
                .maxConcurrentDeliveries(savedPartner.getMaxConcurrentDeliveries())
                .joinedAt(savedPartner.getJoinedAt())
                .build();
    }
}