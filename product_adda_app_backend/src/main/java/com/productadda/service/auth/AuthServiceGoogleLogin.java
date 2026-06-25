package com.productadda.service.auth;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.productadda.dto.auth.GoogleLoginRequestDto;
import com.productadda.dto.auth.LoginResponseDto;
import com.productadda.dto.token.TokenDto;
import com.productadda.entity.Role;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.exception.ApiException;
import com.productadda.repository.RoleRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.service.token.RefreshTokenService;
import com.productadda.service.token.TokenProvider.JwtService;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceGoogleLogin {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final PasswordEncoder passwordEncoder;
    private final UuidUtil uuidUtil;
    private final GoogleIdTokenVerifier googleTokenVerifier;

    /*
     * ================================================================
     * GOOGLE LOGIN ENGINE
     * ================================================================
     */
    @Transactional
    public LoginResponseDto googleLogin(GoogleLoginRequestDto request) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (request == null || request.getIdToken() == null || request.getIdToken().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Google ID Token must not be null or empty");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION / TOKEN VERIFICATION
        // ==========================================
        GoogleIdToken idToken;
        try {
            idToken = googleTokenVerifier.verify(request.getIdToken());
        } catch (Exception e) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Google token parsing failed");
        }

        if (idToken == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid Google ID Token");
        }

        GoogleIdToken.Payload payload = idToken.getPayload();
        String email = payload.getEmail();
        String googleId = payload.getSubject();

        // Extract naming metadata cleanly into final variables to maintain thread
        // safety and lambda boundaries
        String rawFirstName = (String) payload.get("given_name");
        final String finalFirstName = (rawFirstName != null && !rawFirstName.trim().isEmpty())
                ? rawFirstName
                : (String) payload.get("name");

        String rawLastName = (String) payload.get("family_name");
        final String finalLastName = (rawLastName != null) ? rawLastName : "";

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Evaluated on runtime branch inside business workflow processing block below.

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);
        User user;

        // 2.1 Conditional Auto-Provisioning Pipeline Check
        if (!userRepository.existsByEmail(email)) {

            User newUserInstance = User.builder()
                    .pkUserId(uuidUtil.generateUuidV7())
                    .firstName(finalFirstName)
                    .lastName(finalLastName)
                    .email(email)
                    .googleId(googleId)
                    .passwordHash(passwordEncoder.encode(googleId))
                    .emailVerified(true)
                    .isActive(true)
                    .createdAtUtc(nowUtc)
                    .updatedAtUtc(nowUtc)
                    .build();

            user = userRepository.save(newUserInstance);

            Role defaultRole = roleRepository.findByRoleName("USER")
                    .orElseThrow(
                            () -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Default User Role not found"));

            UserRole newUserRoleMapping = UserRole.builder()
                    .pkUserRoleId(uuidUtil.generateUuidV7())
                    .fkUser(user)
                    .fkRole(defaultRole)
                    .createdAtUtc(nowUtc)
                    .isActive(true)
                    .build();

            userRoleRepository.save(newUserRoleMapping);
        } else {
            user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User mapping not found"));
        }

        // 2.2 Role Resolution And Security Mapping Extraction
        List<UserRole> userRoles = userRoleRepository.findByFkUser(user);
        if (userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "User role mapping not found");
        }

        List<String> assignedRoles = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .collect(Collectors.toList());

        boolean isAdmin = assignedRoles.stream()
                .anyMatch(name -> "SUPER_ADMIN".equals(name) || "ADMIN".equals(name));

        if (isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Admin portal login is required");
        }

        // 2.3 Account Lifecycle Constraints Guards
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        // 2.4 Token Generation Pipeline Engine
        String accessToken = jwtService.generateAccessToken(user.getEmail());
        String refreshToken = refreshTokenService.createRefreshToken(user);

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
                .message("Google login successful")
                .build();
    }
}