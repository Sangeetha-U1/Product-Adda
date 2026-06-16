package com.productadda.service.auth;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.productadda.dto.auth.LoginRequestDto;
import com.productadda.dto.auth.LoginResponseDto;
import com.productadda.dto.token.TokenDto;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;

import com.productadda.entity.UserRole;
import com.productadda.repository.UserRoleRepository;
import com.productadda.service.token.RefreshTokenService;
import com.productadda.service.token.TokenProvider;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceLogin {

    /*
     * ================================================================
     * REPOSITORIES
     * ================================================================
     */
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    /*
     * ================================================================
     * SECURITY
     * ================================================================
     */
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider.JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    /*
     * ================================================================
     * LOGIN USER
     * ================================================================
     */
    public LoginResponseDto login(LoginRequestDto request) {

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
         * 3. ROLE VALIDATION
         * ============================================================
         */
        String roleName = userRoles.get(0).getFkRole().getRoleName();

        if ("SUPER_ADMIN".equals(roleName) || "ADMIN".equals(roleName)) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Admin portal login is required");
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
                user.getEmail());

        String refreshToken = refreshTokenService.createRefreshToken(
                user);

        TokenDto token = TokenDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();

        /*
         * ============================================================
         * 6. RESPONSE
         * ============================================================
         */
        return LoginResponseDto.builder()
                .userId(user.getPkUserId())
                .email(user.getEmail())
                .roleName(userRoles.get(0).getFkRole().getRoleName())
                .token(token)
                .message("Login successful")
                .build();
    }
}