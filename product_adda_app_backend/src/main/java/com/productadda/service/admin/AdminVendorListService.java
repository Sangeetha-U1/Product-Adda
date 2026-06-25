package com.productadda.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.admin.VendorAdminSummaryResponseDto;
import com.productadda.exception.ApiException;
import com.productadda.repository.VendorRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminVendorListService {

    /*
     * ================================================================
     * REPOSITORIES
     * ================================================================
     */
    private final VendorRepository vendorRepository;

    /*
     * ================================================================
     * FETCH PAGINATED VENDORS Summary Grid
     * ================================================================
     */
    @Transactional(readOnly = true)
    public Page<VendorAdminSummaryResponseDto> getVendors(Pageable pageable) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (pageable == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Pagination parameters must not be null");
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
        // Note: For a paginated list fetch, global lookup bounds are verified during
        // execution.
        // Any specific database-level rule check before execution goes here.

        /*
         * ================================================================
         * 2. DATABASE LOOKUP SECTION
         * ================================================================
         */
        Page<VendorAdminSummaryResponseDto> vendorSummaryPage = vendorRepository.findAllAdminSummaries(pageable);

        /*
         * ================================================================
         * 3. BUSINESS RULES & PROCESSING
         * Description: Empty check guard and data verification before return
         * ================================================================
         */
        if (vendorSummaryPage == null) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Vendor database ledger returned an invalid null page object");
        }

        /*
         * ================================================================
         * 4. RESPONSE MAPPING
         * ================================================================
         */
        return vendorSummaryPage;
    }
}