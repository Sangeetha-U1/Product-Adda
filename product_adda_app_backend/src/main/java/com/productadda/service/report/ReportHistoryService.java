package com.productadda.service.report;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
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

import com.productadda.dto.report.ReportHistoryItemDto;
import com.productadda.dto.report.ReportHistoryResponseDto;
import com.productadda.entity.Report;
import com.productadda.entity.ReportJobStatus;
import com.productadda.entity.ReportType;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.exception.ApiException;
import com.productadda.repository.ReportJobStatusRepository;
import com.productadda.repository.ReportRepository;
import com.productadda.repository.ReportTypeRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportHistoryService {

    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final int MAX_PAGE_SIZE = 100;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ReportRepository reportRepository;
    private final ReportJobStatusRepository reportJobStatusRepository;
    private final ReportTypeRepository reportTypeRepository;

    // Opaque cursor position: (createdAtUtc, pkReportId) tiebreak pair. Kept
    // as a private helper type — this method still exposes exactly one
    // public entry point, getReportHistory().
    private record CursorPosition(LocalDateTime createdAtUtc, UUID reportId) {
    }

    /*
     * ================================================================
     * 1. VALIDATION SECTION (1.1 Request, 1.2 Auth/Context, 1.3 DB Lookup)
     * ================================================================
     * 2. BUSINESS RULES & PROCESSING / WORKFLOW
     * ================================================================
     * 3. DB SAVING SECTION (Skipped: read-only history query)
     * ================================================================
     * 4. POST-SAVING DATA SANITIZATION & MASKING (Skipped: history payload
     * carries no PII/sensitive fields)
     * ================================================================
     * 5. RESPONSE MAPPING
     * ================================================================
     * Description: Returns a cursor-paginated, newest-first history of
     * report jobs, optionally filtered by status and report type. Admins
     * see every job; all other users see only jobs they personally
     * initiated (fk_user_id match).
     * ================================================================
     */
    @Transactional(readOnly = true)
    public ReportHistoryResponseDto getReportHistory(String statusFilter, String reportTypeFilter, String cursor,
            Integer limit) {

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        int pageSize = limit == null ? DEFAULT_PAGE_SIZE : limit;
        if (pageSize < 1 || pageSize > MAX_PAGE_SIZE) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "limit must be between 1 and " + MAX_PAGE_SIZE);
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

        // Tenant isolation: non-admins only ever see their own jobs. Passing
        // the resolved User entity (rather than a raw UUID) into the query
        // sidesteps any assumption about the entity's PK field name.
        User scopedUser = isAdmin ? null : currentUser;

        ReportJobStatus jobStatus = null;
        if (statusFilter != null && !statusFilter.trim().isEmpty()) {
            jobStatus = reportJobStatusRepository.findByStatusName(statusFilter)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                            "Unknown status filter: " + statusFilter));
        }

        ReportType reportType = null;
        if (reportTypeFilter != null && !reportTypeFilter.trim().isEmpty()) {
            reportType = reportTypeRepository.findByReportTypeName(reportTypeFilter)
                    .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                            "Unknown reportType filter: " + reportTypeFilter));
        }

        // ==========================================
        // 2. BUSINESS RULES & PROCESSING
        // ==========================================
        // Fetch one extra row beyond the page size to detect whether a
        // further page exists, without a separate COUNT query.
        Pageable pageable = PageRequest.of(0, pageSize + 1);

        LocalDateTime cursorCreatedAtUtc = cursorPosition == null ? null : cursorPosition.createdAtUtc();
        UUID cursorReportId = cursorPosition == null ? null : cursorPosition.reportId();

        List<Report> rows = reportRepository.findHistoryPage(scopedUser, jobStatus, reportType,
                cursorCreatedAtUtc, cursorReportId, pageable);

        boolean hasMore = rows.size() > pageSize;
        List<Report> pageRows = hasMore ? rows.subList(0, pageSize) : rows;

        // ==========================================
        // 5. RESPONSE MAPPING
        // ==========================================
        List<ReportHistoryItemDto> items = new ArrayList<>();
        for (Report report : pageRows) {
            items.add(ReportHistoryItemDto.builder()
                    .jobId(report.getPkReportId())
                    .reportName(report.getReportName())
                    .reportType(report.getFkReportType().getReportTypeName())
                    .status(report.getFkStatus().getStatusName())
                    .format(report.getFkExportFormat().getFormatName())
                    .createdAt(report.getCreatedAtUtc())
                    .completedAt(report.getCompletedAtUtc())
                    .fileSizeBytes(report.getFileSizeBytes())
                    .build());
        }

        String nextCursor = null;
        if (hasMore && !pageRows.isEmpty()) {
            Report lastRow = pageRows.get(pageRows.size() - 1);
            nextCursor = encodeCursor(lastRow.getCreatedAtUtc(), lastRow.getPkReportId());
        }

        return ReportHistoryResponseDto.builder()
                .items(items)
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .build();
    }

    // Encodes the pagination boundary as an opaque, URL-safe base64 token.
    private String encodeCursor(LocalDateTime createdAtUtc, UUID reportId) {
        String raw = createdAtUtc.toString() + "|" + reportId.toString();
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    // Decodes an opaque cursor token back into its boundary components.
    private CursorPosition decodeCursor(String cursor) {
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 2);
            LocalDateTime createdAtUtc = LocalDateTime.parse(parts[0]);
            UUID reportId = UUID.fromString(parts[1]);
            return new CursorPosition(createdAtUtc, reportId);
        } catch (Exception ex) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Invalid cursor value");
        }
    }
}
