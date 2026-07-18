package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.InventoryAnalyticsReportRequestDto;
import com.productadda.dto.report.ReportJobStatusDto;
import com.productadda.service.report.InventoryAnalyticsReportInitiationService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class InventoryAnalyticsReportController {

    private final InventoryAnalyticsReportInitiationService inventoryAnalyticsReportInitiationService;

    // ==========================================
    // INITIATE INVENTORY ANALYTICS REPORT
    // Description: Creates an async report generation job (PENDING status)
    // for an inventory analytics report. Admin or vendor — admins may view
    // all/multiple vendors, vendors are hard-scoped to their own products.
    // No date range: reflects current stock/velocity state.
    // Clean Layering: Offloaded to InventoryAnalyticsReportInitiationService.
    // ==========================================
    @PostMapping("/api/reports/inventory-analytics")
    public ResponseEntity<ApiSuccessResponseDto<ReportJobStatusDto>> initiateInventoryAnalyticsReport(
            @Valid @RequestBody InventoryAnalyticsReportRequestDto request) {

        ReportJobStatusDto response = inventoryAnalyticsReportInitiationService
                .initiateInventoryAnalyticsReport(request);

        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiSuccessResponseDto.<ReportJobStatusDto>builder()
                .success(true)
                .message("Report generation initiated")
                .data(response)
                .build());
    }
}
