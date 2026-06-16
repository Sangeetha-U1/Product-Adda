package com.productadda.service.auth;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.f4b6a3.uuid.UuidCreator;
import com.productadda.dto.auth.RegisterRequestDto;
import com.productadda.dto.auth.RegisterResponseDto;
import com.productadda.entity.Role;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.exception.ApiException;
import com.productadda.repository.RoleRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceRegister {

        private final UserRepository userRepository;
        private final RoleRepository roleRepository;
        private final UserRoleRepository userRoleRepository;
        private final PasswordEncoder passwordEncoder;

        public RegisterResponseDto register(RegisterRequestDto request) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Validates incoming request data and verifies
                 * required database constraints before processing registration.
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // Description: Validates incoming registration payload.
                // ==========================================

                if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "First name is required");
                }

                if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Last name is required");
                }

                if (request.getEmail() == null || request.getEmail().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Email is required");
                }

                if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Password is required");
                }

                // ==========================================
                // 1.2 DATABASE LOOKUP VALIDATION
                // Description: Verifies uniqueness constraints before
                // creating new records.
                // ==========================================

                if (userRepository.existsByEmail(request.getEmail())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Email already exists");
                }

                if (request.getMobile() != null
                                && !request.getMobile().trim().isEmpty()
                                && userRepository.existsByMobile(request.getMobile())) {

                        throw new ApiException(HttpStatus.BAD_REQUEST, "Mobile already exists");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION
                 * Description: Executes registration business rules,
                 * role resolution, password encryption, and entity creation.
                 * ================================================================
                 */

                // ==========================================
                // 2.1 ROLE RESOLUTION
                // Description: Determines which role should be
                // assigned to the new account.
                // ==========================================

                String requestedRole = request.getRoleName();
                String roleName;

                if (requestedRole == null || requestedRole.isBlank()) {

                        roleName = "USER";

                } else {

                        requestedRole = requestedRole.toUpperCase();

                        if (requestedRole.equals("ADMIN")
                                        || requestedRole.equals("SUPER_ADMIN")) {

                                throw new ApiException(
                                                HttpStatus.FORBIDDEN,
                                                "You are not allowed to assign ADMIN or SUPER_ADMIN roles");
                        }

                        roleName = requestedRole;
                }

                Role role = roleRepository.findByRoleName(roleName)
                                .orElseThrow(() -> new ApiException(
                                                HttpStatus.BAD_REQUEST,
                                                "Invalid role: " + roleName));

                // ==========================================
                // 2.2 USER ENTITY CREATION
                // Description: Creates user entity and encrypts
                // password before persistence.
                // ==========================================

                User userInstance = User.builder()
                                .pkUserId(UuidCreator.getTimeOrderedEpoch())
                                .firstName(request.getFirstName())
                                .lastName(request.getLastName())
                                .email(request.getEmail())
                                .mobile(request.getMobile())
                                .passwordHash(passwordEncoder.encode(request.getPassword()))
                                // TODO: flip this to false after email verification is implemented.
                                .isActive(true)
                                .build();

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * Description: Persists user records and role mappings.
                 * ================================================================
                 */

                // ==========================================
                // 3.1 USER SAVE
                // Description: Saves user record.
                // ==========================================

                User user = userRepository.save(userInstance);

                // ==========================================
                // 3.2 USER ROLE SAVE
                // Description: Saves user-role relationship.
                // ==========================================

                UserRole userRole = UserRole.builder()
                                .pkUserRoleId(UuidCreator.getTimeOrderedEpoch())
                                .fkUser(user)
                                .fkRole(role)
                                .build();

                userRoleRepository.save(userRole);

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * Description: Maps persisted entities into
                 * registration response payload.
                 * ================================================================
                 */

                // ==========================================
                // 4.1 RESPONSE MAPPING
                // Description: Builds registration response DTO.
                // ==========================================

                return RegisterResponseDto.builder()
                                .userId(user.getPkUserId())
                                .email(user.getEmail())
                                .roleName(role.getRoleName())
                                .build();
        }
}
