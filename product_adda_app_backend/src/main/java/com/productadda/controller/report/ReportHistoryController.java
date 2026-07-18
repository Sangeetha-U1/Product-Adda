package com.productadda.controller.report;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.ReportHistoryResponseDto;
import com.productadda.service.report.ReportHistoryService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReportHistoryController {

    private final ReportHistoryService reportHistoryService;

    // ==========================================
    // GET REPORT HISTORY (CURSOR PAGINATED)
    // Description: Returns a cursor-paginated, newest-first list of report
    // jobs, optionally filtered by status and reportType. Admins see every
    // job; all other users see only jobs they personally initiated.
    // Clean Layering: Offloaded to ReportHistoryService.
    // ==========================================
    @GetMapping("/api/reports/history")
    public ResponseEntity<ApiSuccessResponseDto<ReportHistoryResponseDto>> getReportHistory(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String reportType,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {

        ReportHistoryResponseDto response = reportHistoryService.getReportHistory(status, reportType, cursor, limit);

        return ResponseEntity.ok(ApiSuccessResponseDto.<ReportHistoryResponseDto>builder()
                .success(true)
                .message("Report history retrieved")
                .data(response)
                .build());
    }
}
