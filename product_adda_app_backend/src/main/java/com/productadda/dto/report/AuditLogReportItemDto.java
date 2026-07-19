package com.productadda.dto.report;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogReportItemDto {

    private UUID auditLogId;

    private UUID userId;

    private String userName;

    // Masked to first 3 characters + "***" + domain, per Day 5 spec section 5.1
    private String maskedUserEmail;

    private UUID reportId;

    // Derived via fkReportId -> reports join, not stored directly on
    // report_audit_logs (see week_9_execution_plan_v2.md report_audit_logs
    // table notes)
    private String reportType;

    private String action;

    private Boolean isSuccessful;

    // TODO: always null today - ReportAuditLogService does not yet receive
    // an HttpServletRequest to capture ip_address at write time
    private String ipAddress;

    // TODO: always null today - same capture gap as ipAddress above
    private String userAgent;

    private LocalDateTime createdAt;
}
