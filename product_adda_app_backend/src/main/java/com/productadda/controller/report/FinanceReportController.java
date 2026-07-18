package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.ReportJobStatusDto;
import com.productadda.dto.report.ReportRequestDto;
import com.productadda.service.report.FinanceReconciliationReportInitiationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class FinanceReportController {

    private final FinanceReconciliationReportInitiationService financeReconciliationReportInitiationService;

    // ==========================================
    // INITIATE FINANCE RECONCILIATION REPORT
    // Description: Creates an async report generation job (PENDING status)
    // for a finance reconciliation report. Admin only (no dedicated FINANCE
    // role exists yet). Supports an optional report_depth filter.
    // Clean Layering: Offloaded to FinanceReconciliationReportInitiationService.
    // ==========================================
    @PostMapping("/api/reports/finance/reconciliation")
    public ResponseEntity<ApiSuccessResponseDto<ReportJobStatusDto>> initiateReconciliationReport(
            @Valid @RequestBody ReportRequestDto request) {

        ReportJobStatusDto response = financeReconciliationReportInitiationService
                .initiateReconciliationReport(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiSuccessResponseDto.<ReportJobStatusDto>builder()
                .success(true)
                .message("Report generation initiated")
                .data(response)
                .build());
    }
}
