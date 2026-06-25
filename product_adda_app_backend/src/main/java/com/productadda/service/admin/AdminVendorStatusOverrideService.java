package com.productadda.service.admin;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.admin.VendorStatusOverrideRequestDto;
import com.productadda.dto.admin.VendorStatusOverrideResponseDto;
import com.productadda.entity.Vendor;
import com.productadda.exception.ApiException;
import com.productadda.repository.VendorRepository;
import com.productadda.service.token.RefreshTokenService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminVendorStatusOverrideService {

    /*
     * ================================================================
     * REPOSITORIES & SERVICES
     * ================================================================
     */
    private final VendorRepository vendorRepository;
    private final RefreshTokenService refreshTokenService;

    /*
     * ================================================================
     * OVERRIDE VENDOR STATUS LIFE-CYCLE
     * ================================================================
     */
    @Transactional
    public VendorStatusOverrideResponseDto overrideStatus(UUID vendorId, VendorStatusOverrideRequestDto request) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (vendorId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Vendor identification index parameter must not be null");
        }

        if (request == null || request.getIsActive() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Operational visibility target flag must not be null");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Security context is missing raw authentication data");
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        Vendor vendor = vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target vendor record not found"));

        /*
         * ================================================================
         * 2. BUSINESS WORKFLOW & SESSION EVICTION SECTION
         * ================================================================
         */
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        // Update lifecycle visibility variables cleanly
        vendor.setIsActive(request.getIsActive());
        vendor.setUpdatedAtUtc(nowUtc);

        // Systematically force session termination on suspension
        if (!Boolean.TRUE.equals(request.getIsActive())) {
            refreshTokenService.evictAllUserSessions(vendor.getFkUser());
        }

        /*
         * ================================================================
         * 3. DB SAVING SECTION
         * ================================================================
         */
        vendorRepository.save(vendor);

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return VendorStatusOverrideResponseDto.builder()
                .vendorId(vendor.getPkVendorId())
                .isActive(vendor.getIsActive())
                .build();
    }
}