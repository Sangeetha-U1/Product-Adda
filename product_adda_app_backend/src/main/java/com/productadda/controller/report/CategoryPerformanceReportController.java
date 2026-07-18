package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.ReportJobStatusDto;
import com.productadda.dto.report.ReportRequestDto;
import com.productadda.service.report.CategoryPerformanceReportInitiationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CategoryPerformanceReportController {

    private final CategoryPerformanceReportInitiationService categoryPerformanceReportInitiationService;

    // ==========================================
    // INITIATE CATEGORY PERFORMANCE REPORT
    // Description: Creates an async report generation job (PENDING status)
    // for a category-level sales performance report. Admin or vendor —
    // admins may view all/multiple categories and vendors, vendors are
    // hard-scoped to their own data.
    // Clean Layering: Offloaded to CategoryPerformanceReportInitiationService.
    // ==========================================
    @PostMapping("/api/reports/category-performance")
    public ResponseEntity<ApiSuccessResponseDto<ReportJobStatusDto>> initiateCategoryPerformanceReport(
            @Valid @RequestBody ReportRequestDto request) {

        ReportJobStatusDto response = categoryPerformanceReportInitiationService
                .initiateCategoryPerformanceReport(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiSuccessResponseDto.<ReportJobStatusDto>builder()
                .success(true)
                .message("Report generation initiated")
                .data(response)
                .build());
    }
}
