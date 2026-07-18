package com.productadda.service.report;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.entity.AuditActionType;
import com.productadda.entity.Report;
import com.productadda.entity.ReportAuditLog;
import com.productadda.entity.User;
import com.productadda.exception.ApiException;
import com.productadda.repository.AuditActionTypeRepository;
import com.productadda.repository.ReportAuditLogRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportAuditLogService {

    private final AuditActionTypeRepository auditActionTypeRepository;
    private final ReportAuditLogRepository reportAuditLogRepository;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * 1. VALIDATION SECTION (1.1 Request, 1.2 Auth/Context, 1.3 DB Lookup)
     * ================================================================
     * 2. BUSINESS RULES & PROCESSING / WORKFLOW
     * ================================================================
     * 3. DB SAVING SECTION
     * ================================================================
     * 4. POST-SAVING DATA SANITIZATION & MASKING (Skipped: void return, no
     * response payload to sanitize)
     * ================================================================
     * 5. RESPONSE MAPPING (Skipped: void return, no response DTO to build)
     * ================================================================
     * Description: Persists a single normalized compliance audit trail entry
     * for a report-related action, resolving the action type via the
     * zero-enum lookup table rather than a free-text/enum column.
     * ================================================================
     */
    @Transactional
    public void logReportAction(User actingUser, Report report, String actionTypeName, Boolean isSuccessful) {

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (actingUser == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Acting user is required to log a report action");
        }
        if (report == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Report is required to log a report action");
        }
        if (actionTypeName == null || actionTypeName.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Action type is required to log a report action");
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Not applicable here: this method is invoked internally by other
        // report services, which have already authenticated the caller.

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // TODO: findByActionTypeName is assumed by analogy with
        // PaymentStatusRepository/OrderStatusRepository's findByStatusName
        // pattern. AuditActionTypeRepository.java itself was not reviewed —
        // confirm the exact method name matches before compiling.
        AuditActionType auditActionType = auditActionTypeRepository.findByActionTypeName(actionTypeName)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Unknown audit action type: " + actionTypeName));

        // ==========================================
        // 2. BUSINESS RULES & PROCESSING
        // ==========================================
        // No branching logic required — this is a straight audit-row insert.

        // ==========================================
        // 3. DB SAVING SECTION
        // ==========================================
        // TODO: ip_address/user_agent are not populated — this service is not
        // given an HttpServletRequest. Wire it through from the controller
        // layer in a later day if IP/user-agent capture becomes a requirement.
        ReportAuditLog auditLog = ReportAuditLog.builder()
                .pkReportAuditLogId(uuidUtil.generateUuidV7())
                .fkUser(actingUser)
                .fkReport(report)
                .fkAuditActionType(auditActionType)
                .isSuccessful(isSuccessful)
                .build();

        reportAuditLogRepository.save(auditLog);
    }
}
