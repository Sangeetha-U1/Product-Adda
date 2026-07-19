package com.productadda.controller.report;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.productadda.dto.ApiSuccessResponseDto;
import com.productadda.dto.report.AuditLogReportResponseDto;
import com.productadda.service.report.AuditReportService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AuditReportController {

    private final AuditReportService auditReportService;

    // ==========================================
    // GET REPORT AUDIT LOGS
    // Description: Compliance audit trail for report-related actions
    // (report_audit_logs). Admins see every log, optionally scoped to a
    // single target user via userId; all other authenticated users see
    // only their own logs. Cursor-paginated, newest first.
    // Clean Layering: Offloaded to AuditReportService.
    // ==========================================
    @GetMapping("/api/reports/audit-logs")
    public ResponseEntity<ApiSuccessResponseDto<AuditLogReportResponseDto>> getAuditLogsReport(
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) Integer limit) {

        AuditLogReportResponseDto response = auditReportService.getAuditLogsReport(
                dateFrom, dateTo, action, userId, cursor, limit);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiSuccessResponseDto.<AuditLogReportResponseDto>builder()
                        .success(true)
                        .message("Report audit logs retrieved")
                        .data(response)
                        .build());
    }
}
