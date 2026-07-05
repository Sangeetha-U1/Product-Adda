package com.productadda.service.admin;

import java.util.List;
import java.util.stream.Collectors;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.admin.AdminLoginRequestDto;
import com.productadda.dto.admin.AdminLoginResponseDto;
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
public class AdminLoginService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    private final PasswordEncoder passwordEncoder;
    private final TokenProvider.JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * LOGIN ADMIN
     * ================================================================
     */
    @Transactional
    public AdminLoginResponseDto login(AdminLoginRequestDto request) {

        /*
         * ============================================================
         * 1. VALIDATION SECTION
         * ============================================================
         */
        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================

        if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Email must not be null or empty");
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must not be null or empty");
        }

        /*
         * ============================================================
         * 2. DATABASE LOOKUP
         * ============================================================
         */
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(user);

        if (userRoles.isEmpty()) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "User role not found");
        }

        /*
         * ============================================================
         * 3. ROLE VALIDATION & EXTRACTION
         * Description: Map and compile all assigned multi-role records into a clean
         * string collection array
         * ============================================================
         */
        List<String> assignedRoles = userRoles.stream()
                .map(userRole -> userRole.getFkRole().getRoleName())
                .collect(Collectors.toList());

        boolean hasAdminPrivileges = assignedRoles.stream()
                .anyMatch(name -> "SUPER_ADMIN".equals(name) || "ADMIN".equals(name));

        if (!hasAdminPrivileges) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Access denied: Insufficient administrative privileges to access this console");
        }

        /*
         * ============================================================
         * 4. BUSINESS VALIDATION
         * ============================================================
         */

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Account is inactive");
        }

        if (!Boolean.TRUE.equals(user.getEmailVerified())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Email address not verified");
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        /*
         * ============================================================
         * 5. BUSINESS SECTION
         * Description: Generate JWT access and refresh tokens.
         * ============================================================
         */
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
         * ============================================================
         * 6. RESPONSE
         * ============================================================
         */
        return AdminLoginResponseDto.builder()
                .userId(user.getPkUserId())
                .email(user.getEmail())
                .roles(assignedRoles)
                .token(token)
                .message("Administrative context authenticated successfully")
                .build();
    }
}