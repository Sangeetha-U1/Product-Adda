package com.productadda.service.payment;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.payment.AuditLogResponseDto;
import com.productadda.dto.payment.PaginatedAuditLogResponseDto;

import com.productadda.entity.PaymentAuditLog;
import com.productadda.exception.ApiException;
import com.productadda.repository.PaymentAuditLogRepository;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * ComplianceAuditLogService
 * GET /api/admin/compliance/audit-logs
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class ComplianceAuditLogService {

    private final PaymentAuditLogRepository paymentAuditLogRepository;

    private static final int MAX_PAGE_SIZE = 1000;

    /*
     * ================================================================
     * GET COMPLIANCE AUDIT LOGS
     * ================================================================
     */
    @Transactional(readOnly = true)
    public PaginatedAuditLogResponseDto getAuditLogs(
            int page, int size, String startDateParam, String endDateParam,
            String action, String actorRole, String paymentIdParam) {

        /*
         * ================================================================
         * 1. VALIDATION SECTION
         * ================================================================
         */

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (page < 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "page must be >= 0");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "size must be > 0 and <= " + MAX_PAGE_SIZE);
        }

        LocalDateTime startDate = null;
        if (startDateParam != null && !startDateParam.trim().isEmpty()) {
            try {
                startDate = LocalDate.parse(startDateParam.trim()).atStartOfDay();
            } catch (DateTimeParseException exception) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "startDate must be a valid ISO date (yyyy-MM-dd)");
            }
        }

        LocalDateTime endDate = null;
        if (endDateParam != null && !endDateParam.trim().isEmpty()) {
            try {
                endDate = LocalDate.parse(endDateParam.trim()).atTime(23, 59, 59);
            } catch (DateTimeParseException exception) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "endDate must be a valid ISO date (yyyy-MM-dd)");
            }
        }

        UUID paymentId = null;
        if (paymentIdParam != null && !paymentIdParam.trim().isEmpty()) {
            try {
                paymentId = UUID.fromString(paymentIdParam.trim());
            } catch (IllegalArgumentException exception) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "paymentId is not a valid identifier");
            }
        }

        String safeAction = (action == null || action.trim().isEmpty()) ? null : action.trim();
        String safeActorRole = (actorRole == null || actorRole.trim().isEmpty()) ? null
                : actorRole.trim().toUpperCase();

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        // Note: @PreAuthorize("hasAnyAuthority('ADMIN', 'SUPER_ADMIN')") on the
        // controller already gates this endpoint - no further check needed here.

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        // Note: nothing to pre-fetch beyond the paginated query below.

        /*
         * ================================================================
         * 2. BUSINESS RULES & PROCESSING / WORKFLOW
         * ================================================================
         */
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAtUtc"));

        Page<PaymentAuditLog> auditLogPage = paymentAuditLogRepository.findAuditLogsWithFilters(
                startDate, endDate, safeAction, safeActorRole, paymentId, pageRequest);

        /*
         * ================================================================
         * 3. DB SAVING SECTION (Skip if Read-Only GET)
         * Reason: Read-only paginated lookup, no mutations performed.
         * ================================================================
         */

        /*
         * ================================================================
         * 4. POST-SAVING DATA SANITIZATION & MASKING
         * ================================================================
         */

        List<AuditLogResponseDto> items = auditLogPage.getContent().stream()
                .map(auditLog -> AuditLogResponseDto.builder()
                        .id(auditLog.getPkAuditLogId().toString())
                        .paymentId(auditLog.getFkPayment() != null ? auditLog.getFkPayment().getPkPaymentId().toString()
                                : null)
                        .refundId(auditLog.getFkRefund() != null ? auditLog.getFkRefund().getPkRefundId().toString()
                                : null)
                        .action(auditLog.getAction())
                        .actorId(auditLog.getFkActor() != null ? auditLog.getFkActor().getPkUserId().toString() : null)
                        .actorName(buildActorName(auditLog))
                        .actorRole(auditLog.getActorRole())
                        .oldStatus(auditLog.getOldStatus())
                        .newStatus(auditLog.getNewStatus())
                        .metadata(maskSensitiveMap(auditLog.getMetadata()))
                        .createdAtUtc(auditLog.getCreatedAtUtc())
                        .build())
                .collect(Collectors.toList());

        /*
         * ================================================================
         * 5. RESPONSE MAPPING
         * ================================================================
         */
        return PaginatedAuditLogResponseDto.builder()
                .content(items)
                .totalElements(auditLogPage.getTotalElements())
                .page(page)
                .size(size)
                .build();
    }

    private String buildActorName(PaymentAuditLog auditLog) {
        if (auditLog.getFkActor() == null) {
            return "System";
        }
        return auditLog.getFkActor().getFirstName() + " " + auditLog.getFkActor().getLastName();
    }

    /*
     * Defense-in-depth redaction only: this project's own audit-log writes
     * never store raw card numbers or CVV in the metadata JSON blob by design.
     * Kept as a safety net rather than trusting that invariant unconditionally.
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> maskSensitiveMap(Map<String, Object> sourceMap) {
        if (sourceMap == null) {
            return null;
        }

        // Copy to prevent accidental modifications to Hibernate session cache
        Map<String, Object> maskedMap = new HashMap<>(sourceMap);

        if (maskedMap.containsKey("cardNumber"))
            maskedMap.put("cardNumber", "***REDACTED***");
        if (maskedMap.containsKey("cvv"))
            maskedMap.put("cvv", "***REDACTED***");

        for (Map.Entry<String, Object> entry : maskedMap.entrySet()) {
            if (entry.getValue() instanceof Map) {
                Map<String, Object> nestedMap = (Map<String, Object>) entry.getValue();
                maskedMap.put(entry.getKey(), maskSensitiveMap(nestedMap));
            }
        }

        return maskedMap;
    }
}
