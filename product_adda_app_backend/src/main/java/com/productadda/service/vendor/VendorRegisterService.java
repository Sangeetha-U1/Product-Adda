package com.productadda.service.vendor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.vendor.VendorRegisterRequestDto;
import com.productadda.dto.vendor.VendorProfileResponseDto;
import com.productadda.entity.User;
import com.productadda.entity.Role;
import com.productadda.entity.UserRole;
import com.productadda.entity.Vendor;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.RoleRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.repository.VendorRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorRegisterService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final VendorRepository vendorRepository;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * REGISTER VENDOR
     * Description: Processes merchant applications, updates user authority sets,
     * and links a new merchant profile with unified temporal tracking points.
     * ================================================================
     */
    @Transactional
    public VendorProfileResponseDto registerVendor(VendorRegisterRequestDto request) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST INPUT VALIDATION
        // ==========================================
        if (request == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Registration data request body cannot be null");
        }
        if (request.getBusinessName() == null || request.getBusinessName().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Business name is required");
        }
        if (request.getStoreName() == null || request.getStoreName().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Store name is required");
        }
        if (request.getGstNumber() == null || request.getGstNumber().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "GST number is required");
        }
        if (request.getBusinessDescription() == null || request.getBusinessDescription().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Business description is required");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        if (SecurityContextHolder.getContext().getAuthentication() == null ||
                SecurityContextHolder.getContext().getAuthentication().getName() == null ||
                SecurityContextHolder.getContext().getAuthentication().getName().trim().isEmpty()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Principal system execution identity missing");
        }
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User userInstance = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Authenticated user profile not found"));

        boolean hasCustomerRole = userRoleRepository.existsByFkUserAndFkRole_RoleName(userInstance, "CUSTOMER");
        if (hasCustomerRole) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Accounts with CUSTOMER roles cannot be promoted to a vendor.");
        }

        boolean hasUserRole = userRoleRepository.existsByFkUserAndFkRole_RoleName(userInstance, "USER");
        if (!hasUserRole) {
            throw new ApiException(HttpStatus.FORBIDDEN,
                    "Only standard base USER accounts are authorized to register as a vendor.");
        }

        if (vendorRepository.existsByFkUser(userInstance)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "User account is already registered as a vendor");
        }

        if (vendorRepository.existsByGstNumber(request.getGstNumber().trim())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "GST number is already registered inside the system");
        }

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        Role vendorRoleInstance = roleRepository.findByRoleName("VENDOR")
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "System configuration error: VENDOR role not instantiated"));

        LocalDateTime synchronousNowUtc = LocalDateTime.now(ZoneOffset.UTC);

        Vendor dynamicVendor = Vendor.builder()
                .pkVendorId(uuidUtil.generateUuidV7())
                .fkUser(userInstance)
                .businessName(request.getBusinessName().trim())
                .storeName(request.getStoreName().trim())
                .gstNumber(request.getGstNumber().trim())
                .businessDescription(request.getBusinessDescription().trim())
                .isActive(true)
                .createdAtUtc(synchronousNowUtc)
                .updatedAtUtc(synchronousNowUtc)
                .build();

        UserRole additionalUserRoleMapping = UserRole.builder()
                .pkUserRoleId(uuidUtil.generateUuidV7())
                .fkUser(userInstance)
                .fkRole(vendorRoleInstance)
                .isActive(true)
                .createdAtUtc(synchronousNowUtc)
                .updatedAtUtc(synchronousNowUtc)
                .build();

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        Vendor persistedVendor = vendorRepository.save(dynamicVendor);

        if (!userRoleRepository.existsByFkUserAndFkRole(userInstance, vendorRoleInstance)) {
            userRoleRepository.save(additionalUserRoleMapping);
        }

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return VendorProfileResponseDto.builder()
                .vendorId(persistedVendor.getPkVendorId())
                .userId(userInstance.getPkUserId())
                .businessName(persistedVendor.getBusinessName())
                .storeName(persistedVendor.getStoreName())
                .gstNumber(persistedVendor.getGstNumber())
                .businessDescription(persistedVendor.getBusinessDescription())
                .isActive(persistedVendor.getIsActive())
                .build();
    }
}