package com.productadda.controller.vendor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.vendor.VendorDashboardMetricsResponseDto;
import com.productadda.dto.vendor.VendorProfileResponseDto;
import com.productadda.dto.vendor.VendorRegisterRequestDto;
import com.productadda.dto.vendor.VendorStatusCheckResponseDto;
import com.productadda.service.vendor.VendorDashboardMetricsService;
import com.productadda.service.vendor.VendorProfileService;
import com.productadda.service.vendor.VendorRegisterService;
import com.productadda.service.vendor.VendorStatusCheckService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/vendors")
@RequiredArgsConstructor
public class VendorController {

        private final VendorRegisterService vendorRegisterService;
        private final VendorProfileService vendorProfileService;
        private final VendorDashboardMetricsService vendorDashboardMetricsService;
        private final VendorStatusCheckService vendorStatusCheckService;

        @PostMapping("/register")
        public ResponseEntity<ApiSuccessResponseDto<VendorProfileResponseDto>> registerVendor(
                        @Valid @RequestBody VendorRegisterRequestDto request) {

                VendorProfileResponseDto responseDataInstance = vendorRegisterService.registerVendor(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<VendorProfileResponseDto>builder()
                                                .success(true)
                                                .message("Vendor application processed and active role assigned successfully")
                                                .data(responseDataInstance)
                                                .build());
        }

        @GetMapping("/profile")
        public ResponseEntity<ApiSuccessResponseDto<VendorProfileResponseDto>> getVendorProfile() {

                VendorProfileResponseDto responseDataInstance = vendorProfileService.getVendorProfile();

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<VendorProfileResponseDto>builder()
                                                .success(true)
                                                .message("Merchant profile fetched successfully")
                                                .data(responseDataInstance)
                                                .build());
        }

        @PutMapping("/profile")
        public ResponseEntity<ApiSuccessResponseDto<VendorProfileResponseDto>> updateVendorProfile(
                        @Valid @RequestBody VendorRegisterRequestDto request) {

                VendorProfileResponseDto responseDataInstance = vendorProfileService.updateVendorProfile(request);

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<VendorProfileResponseDto>builder()
                                                .success(true)
                                                .message("Merchant profile updated successfully")
                                                .data(responseDataInstance)
                                                .build());
        }

        // ==========================================
        // DASHBOARD METRICS ENDPOINT
        // ==========================================
        @GetMapping("/dashboard/metrics")
        @PreAuthorize("hasAuthority('VENDOR')")
        public ResponseEntity<ApiSuccessResponseDto<VendorDashboardMetricsResponseDto>> getDashboardMetrics() {

                VendorDashboardMetricsResponseDto responseDataInstance = vendorDashboardMetricsService.getMetrics();

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<VendorDashboardMetricsResponseDto>builder()
                                                .success(true)
                                                .message("Vendor dashboard metrics compiled successfully")
                                                .data(responseDataInstance)
                                                .build());
        }

        // ==========================================
        // STATUS CHECK ENDPOINT
        // ==========================================
        @GetMapping("/status-check")
        public ResponseEntity<ApiSuccessResponseDto<VendorStatusCheckResponseDto>> getVendorStatusCheck() {

                VendorStatusCheckResponseDto responseDataInstance = vendorStatusCheckService.checkStatus();

                return ResponseEntity
                                .status(HttpStatus.OK)
                                .body(ApiSuccessResponseDto.<VendorStatusCheckResponseDto>builder()
                                                .success(true)
                                                .message("Vendor status verification checkpoint passed")
                                                .data(responseDataInstance)
                                                .build());
        }
}