package com.productadda.controller.report;

import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.ReportDownloadResponseDto;
import com.productadda.service.report.ReportDownloadService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class ReportDownloadController {

    private final ReportDownloadService reportDownloadService;

    // ==========================================
    // DOWNLOAD REPORT FILE
    // Description: Secures a short-lived presigned download URL for a
    // completed report file. Only reports in SUCCESS status are
    // downloadable — 409 Conflict otherwise. Admins may download any
    // report; all other users only their own.
    // Clean Layering: Offloaded to ReportDownloadService.
    // ==========================================
    @PostMapping("/api/reports/{jobId}/download")
    public ResponseEntity<ApiSuccessResponseDto<ReportDownloadResponseDto>> downloadReport(
            @PathVariable UUID jobId) {

        ReportDownloadResponseDto response = reportDownloadService.getReportDownloadUrl(jobId);

        return ResponseEntity.ok(ApiSuccessResponseDto.<ReportDownloadResponseDto>builder()
                .success(true)
                .message("Report download URL generated")
                .data(response)
                .build());
    }
}
