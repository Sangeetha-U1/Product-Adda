package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.ReportJobStatusDto;
import com.productadda.dto.report.ReportRequestDto;
import com.productadda.service.report.AdminSalesReportInitiationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminSalesReportInitiationService adminSalesReportInitiationService;

    // ==========================================
    // INITIATE ADMIN SALES REPORT
    // Description: Creates an async report generation job (PENDING status)
    // for a platform-wide sales report. Admin only. Returns immediately;
    // a background worker processes the job asynchronously.
    // Clean Layering: Offloaded to AdminSalesReportInitiationService.
    // ==========================================
    @PostMapping("/api/reports/admin/sales")
    public ResponseEntity<ApiSuccessResponseDto<ReportJobStatusDto>> initiateSalesReport(
            @Valid @RequestBody ReportRequestDto request) {

        ReportJobStatusDto response = adminSalesReportInitiationService.initiateSalesReport(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiSuccessResponseDto.<ReportJobStatusDto>builder()
                .success(true)
                .message("Report generation initiated")
                .data(response)
                .build());
    }
}
