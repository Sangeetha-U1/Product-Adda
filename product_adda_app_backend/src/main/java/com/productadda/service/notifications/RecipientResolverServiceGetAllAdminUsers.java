package com.productadda.service.notifications;

import java.util.List;

import org.springframework.stereotype.Service;

import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RecipientResolverServiceGetAllAdminUsers {

    private final UserRoleRepository userRoleRepository;

    /*
     * ================================================================
     * GET ALL ADMIN USERS
     * Description: Resolves every active user holding the ADMIN role,
     * via user_roles -> roles, for operational notification fan-out.
     * Per Q4, per-admin opt-out is a Day 2 notification_preferences
     * concern -- this method always returns the full admin set.
     * ================================================================
     */
    public List<User> getAllAdminUsers() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Reason: No input parameters to validate.
         * ================================================================
         */

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        List<UserRole> adminUserRoles = userRoleRepository.findByFkRole_RoleName("ADMIN");

        List<User> adminUsers = adminUserRoles.stream()
                .map(UserRole::getFkUser)
                .filter(user -> user != null)
                .distinct()
                .toList();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * Reason: Read-only resolution, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * Reason: N/A - see section 3.
         * ================================================================
         */

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return adminUsers;
    }
}
