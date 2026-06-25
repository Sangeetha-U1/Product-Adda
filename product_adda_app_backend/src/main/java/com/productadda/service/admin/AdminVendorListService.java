package com.productadda.service.admin;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
         * ============================================================
         * 1. VALIDATION SECTION
         * ============================================================
         */
        // ==========================================
        // 1.1 REQUEST BOUNDS CHECK
        // ==========================================
        if (pageable == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Pagination parameters must not be null");
        }

        /*
         * ============================================================
         * 2. DATABASE LOOKUP & PROJECTION CONSTRUCTOR STREAMING
         * Description: Pulls optimized aggregated join summaries without loading
         * heavy collections or full profiles into memory.
         * ============================================================
         */
        Page<VendorAdminSummaryResponseDto> vendorSummaryPage = vendorRepository.findAllAdminSummaries(pageable);

        /*
         * ============================================================
         * 3. RESPONSE
         * ============================================================
         */
        return vendorSummaryPage;
    }
}