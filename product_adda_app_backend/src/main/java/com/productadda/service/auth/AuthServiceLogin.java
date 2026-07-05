package com.productadda.service.auth;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.auth.LoginRequestDto;
import com.productadda.dto.auth.LoginResponseDto;
import com.productadda.dto.token.TokenDto;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.service.token.RefreshTokenService;
import com.productadda.service.token.TokenProvider;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceLogin {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;
    private final TokenProvider.JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * LOGIN USER
     * ================================================================
     */
    @Transactional
    public LoginResponseDto login(LoginRequestDto request) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email must not be null or empty");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must not be null or empty");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Note: Public anonymous authentication endpoint. No pre-existing security
        // context criteria applies.

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(user);
        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User role not found");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */

        // 2.1 Role Validation Guard Criteria
        List<String> assignedRoles = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .collect(Collectors.toList());

        boolean isAdmin = assignedRoles.stream()
                .anyMatch(name -> "SUPER_ADMIN".equals(name) || "ADMIN".equals(name));

        if (isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin portal login is required");
        }

        // 2.2 Profile Lifecycle and Credential Verifications
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Email address not verified");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        // ==========================================
        // 2.3 Cryptographic Token Production Engine
        // ==========================================
        // Pass user details and verified roles directly from your trusty DB source
        String accessToken = jwtService.generateAccessToken(
                user.getPkUserId(),
                user.getEmail(),
                assignedRoles);

        UUID tokenFamilyId = uuidUtil.generateUuidV7();

        String refreshToken = refreshTokenService.createRefreshToken(user, tokenFamilyId);

        TokenDto token = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Note: Handled implicitly downstream within RefreshTokenService transaction
         * lifecycle boundary.
         * ================================================================
         */

        // TODO: init cart and wishlist tables with empty.

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return LoginResponseDto.builder()
                .userId(user.getPkUserId())
                .email(user.getEmail())
                .roles(assignedRoles)
                .token(token)
                .message("Login successful")
                .build();
    }
}