package com.productadda.service.vendor;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.vendor.VendorProfileResponseDto;
import com.productadda.dto.vendor.VendorRegisterRequestDto;
import com.productadda.entity.User;
import com.productadda.entity.Vendor;
import com.productadda.exception.ApiException;
import com.productadda.repository.UserRepository;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VendorProfileService {

        private final UserRepository userRepository;
        private final VendorRepository vendorRepository;

        /*
         * ================================================================
         * GET VENDOR PROFILE
         * Description: Locates and compiles core registration data for the
         * authenticated vendor session principal.
         * ================================================================
         */
        @Transactional(readOnly = true)
        public VendorProfileResponseDto getVendorProfile() {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                // Pure contextual database lookup read stream. No payload attributes to filter.

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
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user profile not found"));

                Vendor targetedVendorProfile = vendorRepository.findByFkUser(userInstance)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No associated vendor profile found for this user account"));

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                // Structural state validation verified successfully. Proceeding to direct view
                // conversion.

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
                 * ================================================================
                 */
                return VendorProfileResponseDto.builder()
                                .vendorId(targetedVendorProfile.getPkVendorId())
                                .userId(userInstance.getPkUserId())
                                .businessName(targetedVendorProfile.getBusinessName())
                                .storeName(targetedVendorProfile.getStoreName())
                                .gstNumber(targetedVendorProfile.getGstNumber())
                                .businessDescription(targetedVendorProfile.getBusinessDescription())
                                .isActive(targetedVendorProfile.getIsActive())
                                .build();
        }

        /*
         * ================================================================
         * UPDATE VENDOR PROFILE
         * Description: Enforces conflict checks against shared brand labels and mutates
         * underlying merchant metadata properties.
         * ================================================================
         */
        @Transactional
        public VendorProfileResponseDto updateVendorProfile(VendorRegisterRequestDto requestDto) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * ================================================================
                 */

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
                if (requestDto == null) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Profile mutation modification body cannot be null");
                }
                if (requestDto.getBusinessName() == null || requestDto.getBusinessName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST, "Business name cannot be blank");
                }
                if (requestDto.getStoreName() == null || requestDto.getStoreName().trim().isEmpty()) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Store storefront identifier label cannot be blank");
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
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user profile not found"));

                Vendor vendor = vendorRepository.findByFkUser(userInstance)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No associated vendor profile found for this user account"));

                if (vendorRepository.existsByBusinessNameAndPkVendorIdNot(requestDto.getBusinessName().trim(),
                                vendor.getPkVendorId())) {
                        throw new ApiException(HttpStatus.BAD_REQUEST,
                                        "Business name is already registered by another vendor listing");
                }

                /*
                 * ================================================================
                 * 2. BUSINESS RULES & PROCESSING / WORKFLOW
                 * ================================================================
                 */
                vendor.setBusinessName(requestDto.getBusinessName().trim());
                vendor.setStoreName(requestDto.getStoreName().trim());
                if (requestDto.getBusinessDescription() != null) {
                        vendor.setBusinessDescription(requestDto.getBusinessDescription().trim());
                }

                /*
                 * ================================================================
                 * 3. DB SAVING SECTION
                 * ================================================================
                 */
                Vendor updatedVendor = vendorRepository.save(vendor);

                /*
                 * ================================================================
                 * 4. RESPONSE MAPPING
                 * ================================================================
                 */
                return VendorProfileResponseDto.builder()
                                .vendorId(updatedVendor.getPkVendorId())
                                .userId(userInstance.getPkUserId())
                                .businessName(updatedVendor.getBusinessName())
                                .storeName(updatedVendor.getStoreName())
                                .gstNumber(updatedVendor.getGstNumber())
                                .businessDescription(updatedVendor.getBusinessDescription())
                                .isActive(updatedVendor.getIsActive())
                                .build();
        }
}