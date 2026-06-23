package com.productadda.service.vendor;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.vendor.VendorProfileResponseDto;
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

        // perfectly safe there because that method only runs SELECT queries.
        @Transactional(readOnly = true)
        public VendorProfileResponseDto getVendorProfile(String currentUserEmail) {

                /*
                 * ================================================================
                 * 1. VALIDATION SECTION
                 * Description: Resolves the active caller security context and asserts
                 * matching target vendor database linkages exist.
                 * ================================================================
                 */

                User userInstance = userRepository.findByEmail(currentUserEmail)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "Authenticated user profile not found"));

                /*
                 * ================================================================
                 * 2. BUSINESS SECTION / 3. DB SAVING SECTION (Read-Only)
                 * Description: Performs index lookup to pull operational row states.
                 * ================================================================
                 */

                Vendor targetedVendorProfile = vendorRepository.findByFkUser(userInstance)
                                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                "No associated vendor profile found for this user account"));

                /*
                 * ================================================================
                 * 4. RESPONSE SECTION
                 * Description: Maps target properties seamlessly into a clean return object.
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
}