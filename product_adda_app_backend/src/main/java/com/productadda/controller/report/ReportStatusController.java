package com.productadda.controller.report;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.ReportStatusResponseDto;
import com.productadda.service.report.ReportStatusPollingService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReportStatusController {

    private final ReportStatusPollingService reportStatusPollingService;

    // ==========================================
    // GET REPORT JOB STATUS
    // Description: Returns the current processing status of a single
    // report job for polling. Admins may poll any job; all other users
    // may only poll jobs they personally initiated.
    // Clean Layering: Offloaded to ReportStatusPollingService.
    // ==========================================
    @GetMapping("/api/reports/{jobId}")
    public ResponseEntity<ApiSuccessResponseDto<ReportStatusResponseDto>> getReportStatus(
            @PathVariable UUID jobId) {

        ReportStatusResponseDto response = reportStatusPollingService.getReportStatus(jobId);

        return ResponseEntity.ok(ApiSuccessResponseDto.<ReportStatusResponseDto>builder()
                .success(true)
                .message("Report status retrieved")
                .data(response)
                .build());
    }
}
