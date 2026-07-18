package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.ReportJobStatusDto;
import com.productadda.dto.report.ReportRequestDto;
import com.productadda.service.report.VendorEarningsReportInitiationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VendorReportController {

    private final VendorEarningsReportInitiationService vendorEarningsReportInitiationService;

    // ==========================================
    // INITIATE VENDOR EARNINGS REPORT
    // Description: Creates an async report generation job (PENDING status)
    // for a vendor-scoped earnings report. Vendor only, hard tenant
    // isolated — the vendor id is always resolved server-side from the
    // authenticated user, never trusted from the request body.
    // Clean Layering: Offloaded to VendorEarningsReportInitiationService.
    // ==========================================
    @PostMapping("/api/reports/vendor/earnings")
    public ResponseEntity<ApiSuccessResponseDto<ReportJobStatusDto>> initiateEarningsReport(
            @Valid @RequestBody ReportRequestDto request) {

        ReportJobStatusDto response = vendorEarningsReportInitiationService.initiateEarningsReport(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiSuccessResponseDto.<ReportJobStatusDto>builder()
                .success(true)
                .message("Report generation initiated")
                .data(response)
                .build());
    }
}
