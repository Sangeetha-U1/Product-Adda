package com.productadda.service.vendor;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.vendor.VendorStatusCheckResponseDto;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorStatusCheckService {

    private final UserRepository userRepository;
    private final VendorRepository vendorRepository;

    /*
     * ================================================================
     * CHECK VENDOR STATUS
     * Description: Lightweight gateway checkpoint for UI router stability.
     * Handles missing vendor profiles cleanly without throwing errors.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public VendorStatusCheckResponseDto checkStatus() {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * Description: Resolves caller context identities and handles soft
         * evaluation of vendor optional registries without throwing.
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        // Context-driven identifier lookup. No payload elements to evaluate.

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

        // ==========================================
        // VENDOR PROFILE SOFT LOOKUP
        // ==========================================
        java.util.Optional<Vendor> vendorOptional = vendorRepository.findByFkUser(userInstance);

        /*
         * ================================================================
         * 2. BUSINESS SECTION
         * Description: Evaluates the presence or absence of vendor profiles
         * to dynamically establish router permissions.
         * ================================================================
         */
        boolean isVendorPresent = vendorOptional.isPresent();
        UUID activeVendorId = null;
        boolean isVendorActive = false;

        if (isVendorPresent) {
            Vendor vendorInstance = vendorOptional.get();
            activeVendorId = vendorInstance.getPkVendorId();
            isVendorActive = Boolean.TRUE.equals(vendorInstance.getIsActive());
        }

        /*
         * ================================================================
         * 4. POST-PROCESSING EVALUATION & STATE DETERMINATION
         * ================================================================
         */
        // Business state evaluations parsed and finalized. Ready for DTO container
        // routing.

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        if (isVendorPresent) {
            return VendorStatusCheckResponseDto.builder()
                    .vendorId(activeVendorId)
                    .isVendor(true)
                    .isActive(isVendorActive)
                    .build();
        }

        return VendorStatusCheckResponseDto.builder()
                .vendorId(null)
                .isVendor(false)
                .isActive(false)
                .build();
    }
}