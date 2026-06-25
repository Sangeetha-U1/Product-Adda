package com.productadda.controller.admin;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.admin.VendorAdminSummaryResponseDto;
import com.productadda.dto.admin.VendorStatusOverrideRequestDto;
import com.productadda.dto.admin.VendorStatusOverrideResponseDto;
import com.productadda.service.admin.AdminVendorListService;
import com.productadda.service.admin.AdminVendorStatusOverrideService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminVendorManagementController {

        private final AdminVendorListService adminVendorListService;
        private final AdminVendorStatusOverrideService adminVendorStatusOverrideService;

        @GetMapping("/vendors")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<Page<VendorAdminSummaryResponseDto>>> getVendors(
                        @PageableDefault(page = 0, size = 10) Pageable pageable) {

                Page<VendorAdminSummaryResponseDto> responseData = adminVendorListService.getVendors(pageable);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<Page<VendorAdminSummaryResponseDto>>builder()
                                                .success(true)
                                                .message("Vendor summary records fetched successfully")
                                                .data(responseData)
                                                .build());
        }

        @PatchMapping("/vendors/{vendorId}/status")
        @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')")
        public ResponseEntity<ApiSuccessResponseDto<VendorStatusOverrideResponseDto>> overrideStatus(
                        @PathVariable UUID vendorId,
                        @Valid @RequestBody VendorStatusOverrideRequestDto request) {

                VendorStatusOverrideResponseDto responseData = adminVendorStatusOverrideService.overrideStatus(vendorId,
                                request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<VendorStatusOverrideResponseDto>builder()
                                                .success(true)
                                                .message("Vendor operational lifecycle status overridden completed and session tokens evicted")
                                                .data(responseData)
                                                .build());
        }
}