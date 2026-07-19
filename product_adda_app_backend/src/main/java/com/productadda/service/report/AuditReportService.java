package com.productadda.service.report;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.report.AuditLogReportItemDto;
import com.productadda.dto.report.AuditLogReportResponseDto;

import com.productadda.entity.AuditActionType;
import com.productadda.entity.ReportAuditLog;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.AuditActionTypeRepository;
import com.productadda.repository.ReportAuditLogRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditReportService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;
    private static final int MAX_DATE_RANGE_DAYS = 365;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ReportAuditLogRepository reportAuditLogRepository;
    private final AuditActionTypeRepository auditActionTypeRepository;

    // Opaque cursor position: (createdAtUtc, pkReportAuditLogId) tiebreak
    // pair, mirroring ReportHistoryService's CursorPosition pattern.
    private record CursorPosition(LocalDateTime createdAtUtc, UUID auditLogId) {
    }

    /*
     * ================================================================
     * 1. VALIDATION SECTION (1.1 Request, 1.2 Auth/Context, 1.3 DB Lookup)
     * ================================================================
     * 2. BUSINESS RULES & PROCESSING / WORKFLOW
     * ================================================================
     * 3. DB SAVING SECTION (Skipped: read-only audit trail query)
     * ================================================================
     * 4. POST-SAVING DATA SANITIZATION & MASKING
     * ================================================================
     * 5. RESPONSE MAPPING
     * ================================================================
     * Description: Returns a cursor-paginated, newest-first compliance
     * audit trail from report_audit_logs. Admins may view every log,
     * optionally scoped to one target user via userId; all other
     * authenticated users may only view their own logs. reportType is
     * derived by joining fk_report_id -> reports, per the zero-enum,
     * normalized audit-log design in week_9_execution_plan_v2.md.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public AuditLogReportResponseDto getAuditLogsReport(String dateFromParam, String dateToParam,
            String actionParam, String userIdParam, String cursor, Integer limit) {

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        int pageSize = limit == null ? DEFAULT_PAGE_SIZE : limit;
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "limit must be between 1 and " + MAX_PAGE_SIZE);
        }

        LocalDateTime dateFrom = parseDateFrom(dateFromParam);
        LocalDateTime dateTo = parseDateTo(dateToParam);
        if (dateFrom != null && dateTo != null) {
            if (dateFrom.isAfter(dateTo)) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "dateFrom must be before dateTo");
            }
            if (ChronoUnit.DAYS.between(dateFrom, dateTo) > MAX_DATE_RANGE_DAYS) {
                throw new ApiException(HttpStatus.BAD_REQUEST,
                        "Date range cannot exceed " + MAX_DATE_RANGE_DAYS + " days");
            }
        }

        CursorPosition cursorPosition = null;
        if (cursor != null && !cursor.trim().isEmpty()) {
            cursorPosition = decodeCursor(cursor);
        }

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }
        String currentUsername = authentication.getName();

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        User currentUser = userRepository.findByEmail(currentUsername)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                        "Authenticated user context no longer exists"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
        boolean isAdmin = userRoles.stream()
                .map(role -> role.getFkRole().getRoleName())
                .anyMatch(name -> "ADMIN".equals(name) || "SUPER_ADMIN".equals(name));

        User scopedUser = resolveScopedUser(isAdmin, currentUser, userIdParam);

        AuditActionType actionType = null;
        if (actionParam != null && !actionParam.trim().isEmpty()) {
            actionType = auditActionTypeRepository.findByActionTypeName(actionParam)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                            "Unknown action filter: " + actionParam));
        }

        // ==========================================
        // 2. BUSINESS RULES & PROCESSING
        // ==========================================
        // Fetch one extra row beyond the page size to detect whether a
        // further page exists, without a separate COUNT query.
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        LocalDateTime cursorCreatedAtUtc = cursorPosition == null ? null : cursorPosition.createdAtUtc();
        UUID cursorAuditLogId = cursorPosition == null ? null : cursorPosition.auditLogId();

        List<ReportAuditLog> rows = reportAuditLogRepository.findAuditLogHistoryPage(scopedUser, actionType,
                dateFrom, dateTo, cursorCreatedAtUtc, cursorAuditLogId, pageable);

        boolean hasMore = rows.size() > pageSize;
        List<ReportAuditLog> pageRows = hasMore ? rows.subList(0, pageSize) : rows;

        // ==========================================
        // 4. POST-SAVING DATA SANITIZATION & MASKING
        // (email masking applied per row in the mapping loop below)
        // ==========================================

        // ==========================================
        // 5. RESPONSE MAPPING
        // ==========================================
        List<AuditLogReportItemDto> items = new ArrayList<>();
        for (ReportAuditLog auditLog : pageRows) {
            User rowUser = auditLog.getFkUser();
            items.add(AuditLogReportItemDto.builder()
                    .auditLogId(auditLog.getPkReportAuditLogId())
                    .userId(rowUser.getPkUserId())
                    .userName(rowUser.getFirstName() + " " + rowUser.getLastName())
                    .maskedUserEmail(maskEmail(rowUser.getEmail()))
                    .reportId(auditLog.getFkReport().getPkReportId())
                    .reportType(auditLog.getFkReport().getFkReportType().getReportTypeName())
                    .action(auditLog.getFkAuditActionType().getActionTypeName())
                    .isSuccessful(auditLog.getIsSuccessful())
                    .ipAddress(auditLog.getIpAddress())
                    .userAgent(auditLog.getUserAgent())
                    .createdAt(auditLog.getCreatedAtUtc())
                    .build());
        }

        String nextCursor = null;
        if (hasMore && !pageRows.isEmpty()) {
            ReportAuditLog lastRow = pageRows.get(pageRows.size() - 1);
            nextCursor = encodeCursor(lastRow.getCreatedAtUtc(), lastRow.getPkReportAuditLogId());
        }

        return AuditLogReportResponseDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    // Resolves which user's logs the caller may see: admins may target any
    // user via userIdParam (or leave it null for an unrestricted view);
    // non-admins are hard-scoped to their own user id and are rejected if
    // they attempt to request a different one.
    private User resolveScopedUser(boolean isAdmin, User currentUser, String userIdParam) {
        if (userIdParam == null || userIdParam.trim().isEmpty()) {
            return isAdmin ? null : currentUser;
        }

        UUID requestedUserId;
        try {
            requestedUserId = UUID.fromString(userIdParam.trim());
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "userId is not a valid identifier");
        }

        if (!isAdmin && !requestedUserId.equals(currentUser.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Cannot view audit logs for other users");
        }

        if (requestedUserId.equals(currentUser.getPkUserId())) {
            return currentUser;
        }

        return userRepository.findById(requestedUserId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Target user not found"));
    }

    // Masks an email to its first 3 characters plus the domain, per Day 5
    // spec section 5.1 ("mask email to first 3 chars").
    private String maskEmail(String email) {
        if (email == null) {
            return null;
        }
        int atIndex = email.indexOf('@');
        if (atIndex <= 0) {
            return "***";
        }
        int visibleChars = Math.min(3, atIndex);
        return email.substring(0, visibleChars) + "***" + email.substring(atIndex);
    }

    private LocalDateTime parseDateFrom(String dateFromParam) {
        if (dateFromParam == null || dateFromParam.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateFromParam.trim()).atStartOfDay();
        } catch (DateTimeParseException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dateFrom must be a valid ISO date (yyyy-MM-dd)");
        }
    }

    private LocalDateTime parseDateTo(String dateToParam) {
        if (dateToParam == null || dateToParam.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateToParam.trim()).atTime(23, 59, 59);
        } catch (DateTimeParseException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "dateTo must be a valid ISO date (yyyy-MM-dd)");
        }
    }

    // Encodes the pagination boundary as an opaque, URL-safe base64 token.
    private String encodeCursor(LocalDateTime createdAtUtc, UUID auditLogId) {
        String raw = createdAtUtc.toString() + "|" + auditLogId.toString();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    // Decodes an opaque cursor token back into its boundary components.
    private CursorPosition decodeCursor(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 2);
            LocalDateTime createdAtUtc = LocalDateTime.parse(parts[0]);
            UUID auditLogId = UUID.fromString(parts[1]);
            return new CursorPosition(createdAtUtc, auditLogId);
        } catch (Exception exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid cursor value");
        }
    }
}
