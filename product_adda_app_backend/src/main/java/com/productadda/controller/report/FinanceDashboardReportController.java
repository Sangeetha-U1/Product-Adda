package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.FinanceDashboardReportRequestDto;
import com.productadda.dto.report.ReportJobStatusDto;
import com.productadda.service.report.FinanceDashboardReportInitiationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FinanceDashboardReportController {

    private final FinanceDashboardReportInitiationService financeDashboardReportInitiationService;

    // ==========================================
    // INITIATE FINANCE DASHBOARD REPORT
    // Description: Creates an async report generation job (PENDING status)
    // for a finance dashboard report. Admin only — no vendor branch exists
    // for this endpoint, unlike Day 3's dual-role report endpoints.
    // Clean Layering: Offloaded to FinanceDashboardReportInitiationService.
    // ==========================================
    @PostMapping("/api/reports/finance-dashboard")
    public ResponseEntity<ApiSuccessResponseDto<ReportJobStatusDto>> initiateFinanceDashboardReport(
            @Valid @RequestBody FinanceDashboardReportRequestDto request) {

        ReportJobStatusDto response = financeDashboardReportInitiationService
                .initiateFinanceDashboardReport(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiSuccessResponseDto.<ReportJobStatusDto>builder()
                .success(true)
                .message("Report generation initiated")
                .data(response)
                .build());
    }
}
