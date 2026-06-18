package com.productadda.service.auth;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.auth.UserProfileResponseDto;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceMe {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    /*
     * ================================================================
     * GET CURRENT PROFILE
     * Description: Retrieves profile details for the currently logged-in context
     * session user.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public UserProfileResponseDto getMe() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Extracts context security principals and asserts user status.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

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
        // 1.2 DATABASE LOOKUP VALIDATION
        // ==========================================
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "Authenticated user no longer exists"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(user);

        if (userRoles == null || userRoles.isEmpty()) {
            throw new ApiException(HttpStatus.NOT_FOUND, "No roles assigned to this user profile");
        }

        // Explicitly extract and validate the role name to ensure it can never be null
        UserRole primaryUserRole = userRoles.get(0);
        if (primaryUserRole.getFkRole() == null || primaryUserRole.getFkRole().getRoleName() == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "User role configuration is broken or missing data");
        }

        String assignedRoleName = primaryUserRole.getFkRole().getRoleName();

        /*
         * ================================================================
         * 4. RESPONSE SECTION
         * Description: Transforms domain model records into public transport
         * representations.
         * ================================================================
         */
        return UserProfileResponseDto.builder()
                .userId(user.getPkUserId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .mobile(user.getMobile())
                .isEmailVerified(user.getEmailVerified())
                .isActive(user.getIsActive())
                .roleName(assignedRoleName)
                .build();
    }
}