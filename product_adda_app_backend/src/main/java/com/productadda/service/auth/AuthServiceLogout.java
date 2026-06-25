package com.productadda.service.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.auth.LogoutRequestDto;
import com.productadda.dto.auth.LogoutResponseDto;
import com.productadda.entity.RefreshToken;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.service.token.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceLogout {

        private final RefreshTokenService refreshTokenService;
        private final UserRepository userRepository;

        /*
         * ================================================================
         * LOGOUT USER
         * Description: Handles the application logout mechanism and invalidates tokens.
         * ================================================================
         */
        @Transactional
        public LogoutResponseDto logout(LogoutRequestDto request) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Pulls context principals and asserts token-to-user mappings.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (request == null || request.getRefreshToken() == null
                                || request.getRefreshToken().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Refresh token parameter must not be null or empty");
                }

                // ==========================================
                // 1.2 CONTEXT AUTHENTICATION
                // ==========================================
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                if (authentication == null || !authentication.isAuthenticated()) {
                        throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
                }

                String email;
                if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                        email = userDetails.getUsername();
                } else {
                        email = authentication.getName();
                }

                // ==========================================
                // 1.3 DATABASE LOOKUP VALIDATION
                // ==========================================
                User user = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user no longer exists"));

                RefreshToken refreshToken = refreshTokenService.validateRefreshToken(request.getRefreshToken());

                if (!refreshToken.getFkUser().getPkUserId().equals(user.getPkUserId())) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Refresh token does not belong to authenticated user");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                // Note: Security context teardown is executed post persistence invalidation.

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Modifies metadata states inside underlying infrastructure
                 * storages.
                 * ================================================================
                 */
                refreshTokenService.revokeRefreshToken(refreshToken);

                // Clear the Security Context explicitly to immediately strip credentials from
                // the local thread context
                // TODO: Are we sure this clear context is correct? because clearing will delete all users context?
                SecurityContextHolder.clearContext();

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
                 * ================================================================
                 */
                return LogoutResponseDto.builder()
                                .email(user.getEmail())
                                .build();
        }
}