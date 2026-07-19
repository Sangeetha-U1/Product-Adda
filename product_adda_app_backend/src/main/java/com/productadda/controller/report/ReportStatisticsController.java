package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.ReportStatisticsResponseDto;
import com.productadda.service.report.ReportStatisticsService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReportStatisticsController {

    private final ReportStatisticsService reportStatisticsService;

    // ==========================================
    // GET REPORT GENERATION STATISTICS
    // Description: Today's report generation metrics - job counts, success
    // rate, generation-time percentiles, per-report-type breakdown, and
    // current load against the configured hourly job capacity. Any
    // authenticated user may call this endpoint.
    // Clean Layering: Offloaded to ReportStatisticsService.
    // ==========================================
    @GetMapping("/api/reports/statistics")
    public ResponseEntity<ApiSuccessResponseDto<ReportStatisticsResponseDto>> getReportStatistics() {

        ReportStatisticsResponseDto response = reportStatisticsService.getReportStatistics();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<ReportStatisticsResponseDto>builder()
                        .success(true)
                        .message("Report statistics retrieved")
                        .data(response)
                        .build());
    }
}
