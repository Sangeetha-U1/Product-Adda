package com.productadda.service.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.productadda.dto.auth.LogoutRequestDto;
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

        public void logout(
                        LogoutRequestDto request) {

                /*
                 * ============================================================
                 * 1. AUTHENTICATED USER
                 * ============================================================
                 */
                Authentication authentication = SecurityContextHolder
                                .getContext()
                                .getAuthentication();

                String email;

                if (authentication.getPrincipal() instanceof UserDetails userDetails) {
                        email = userDetails.getUsername();
                } else {
                        email = authentication.getName();
                }

                User currentUser = userRepository.findByEmail(email)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.NOT_FOUND,
                                                "Authenticated user no longer exists"));

                /*
                 * ============================================================
                 * 2. VALIDATE REFRESH TOKEN
                 * ============================================================
                 */
                RefreshToken refreshToken = refreshTokenService
                                .validateRefreshToken(
                                                request.getRefreshToken());

                /*
                 * ============================================================
                 * 3. OWNERSHIP VALIDATION
                 * ============================================================
                 */
                if (!refreshToken.getFkUser()
                                .getPkUserId()
                                .equals(currentUser.getPkUserId())) {

                        throw new ApiException(
                                        HttpStatus.FORBIDDEN,
                                        "Refresh token does not belong to authenticated user");
                }

                /*
                 * ============================================================
                 * 4. REVOKE TOKEN
                 * ============================================================
                 */
                refreshTokenService.revokeRefreshToken(
                                refreshToken);
        }
}