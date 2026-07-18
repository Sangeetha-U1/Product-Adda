package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.ReportJobStatusDto;
import com.productadda.dto.report.ReportRequestDto;
import com.productadda.service.report.VendorPerformanceReportInitiationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VendorPerformanceReportController {

    private final VendorPerformanceReportInitiationService vendorPerformanceReportInitiationService;

    // ==========================================
    // INITIATE VENDOR PERFORMANCE REPORT
    // Description: Creates an async report generation job (PENDING status)
    // for a vendor performance report. Admin or vendor — admins may view
    // all/multiple vendors, vendors are hard-scoped to their own data.
    // Clean Layering: Offloaded to VendorPerformanceReportInitiationService.
    // ==========================================
    @PostMapping("/api/reports/vendor-performance")
    public ResponseEntity<ApiSuccessResponseDto<ReportJobStatusDto>> initiateVendorPerformanceReport(
            @Valid @RequestBody ReportRequestDto request) {

        ReportJobStatusDto response = vendorPerformanceReportInitiationService
                .initiateVendorPerformanceReport(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiSuccessResponseDto.<ReportJobStatusDto>builder()
                .success(true)
                .message("Report generation initiated")
                .data(response)
                .build());
    }
}
