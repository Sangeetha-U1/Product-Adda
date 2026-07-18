package com.productadda.service.report;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.report.FinanceDashboardReportRequestDto;
import com.productadda.dto.report.ReportJobStatusDto;

import com.productadda.entity.Report;
import com.productadda.entity.ReportExportFormat;
import com.productadda.entity.ReportJobStatus;
import com.productadda.entity.ReportType;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;

import com.productadda.exception.ApiException;

import com.productadda.repository.ReportExportFormatRepository;
import com.productadda.repository.ReportJobStatusRepository;
import com.productadda.repository.ReportRepository;
import com.productadda.repository.ReportTypeRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FinanceDashboardReportInitiationService {

    private static final long MAX_DATE_RANGE_DAYS = 365;
    private static final String REPORT_TYPE_NAME = "FINANCE_DASHBOARD";
    private static final String PENDING_STATUS_NAME = "PENDING";
    private static final String AUDIT_ACTION_NAME = "REPORT_GENERATED";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ReportRepository reportRepository;
    private final ReportTypeRepository reportTypeRepository;
    private final ReportJobStatusRepository reportJobStatusRepository;
    private final ReportExportFormatRepository reportExportFormatRepository;
    private final ReportAuditLogService reportAuditLogService;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * 1. VALIDATION SECTION (1.1 Request, 1.2 Auth/Context, 1.3 DB Lookup)
     * ================================================================
     * 2. BUSINESS RULES & PROCESSING / WORKFLOW
     * ================================================================
     * 3. DB SAVING SECTION
     * ================================================================
     * 4. POST-SAVING DATA SANITIZATION & MASKING (Skipped: job-creation
     * response carries no PII/sensitive fields at this stage)
     * ================================================================
     * 5. RESPONSE MAPPING
     * ================================================================
     * Description: Creates a PENDING async job record for a finance
     * dashboard report. Admin-only — unlike Day 3's dual-role endpoints,
     * there is no vendor branch here at all. FinanceDashboardReportRequestDto
     * has no vendorIds field by design, so admins cannot request a
     * per-vendor breakdown on this report.
     * ================================================================
     */
    @Transactional
    public ReportJobStatusDto initiateFinanceDashboardReport(FinanceDashboardReportRequestDto request) {

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (request.getDateFrom() == null || request.getDateTo() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "date_from and date_to are required");
        }
        if (!request.getDateFrom().isBefore(request.getDateTo())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "date_from must be before date_to");
        }
        long rangeDays = ChronoUnit.DAYS.between(request.getDateFrom(), request.getDateTo());
        if (rangeDays > MAX_DATE_RANGE_DAYS) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Date range cannot exceed 365 days");
        }
        if (request.getFormat() == null || request.getFormat().trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "format is required");
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
        // TODO: Introduce a dedicated FINANCE role in a future week if
        // finance-team access needs separation from general admin access.
        boolean isAdmin = userRoles.stream()
                .map(role -> role.getFkRole().getRoleName())
                .anyMatch(name -> "ADMIN".equals(name) || "SUPER_ADMIN".equals(name));
        if (!isAdmin) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Only admins can generate finance reports");
        }

        ReportType reportType = reportTypeRepository.findByReportTypeName(REPORT_TYPE_NAME)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "FINANCE_DASHBOARD report type not configured"));

        ReportJobStatus pendingStatus = reportJobStatusRepository.findByStatusName(PENDING_STATUS_NAME)
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "PENDING job status not configured"));

        ReportExportFormat exportFormat = reportExportFormatRepository.findByFormatName(request.getFormat())
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Format must be PDF, CSV, or JSON"));

        // ==========================================
        // 2. BUSINESS RULES & PROCESSING
        // ==========================================
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        Map<String, Object> filters = new HashMap<>();
        filters.put("dateFrom", request.getDateFrom().toString());
        filters.put("dateTo", request.getDateTo().toString());

        // ==========================================
        // 3. DB SAVING SECTION
        // ==========================================
        Report reportJob = Report.builder()
                .pkReportId(uuidUtil.generateUuidV7())
                .fkUser(currentUser)
                .fkReportType(reportType)
                .fkVendor(null)
                .fkStatus(pendingStatus)
                .fkExportFormat(exportFormat)
                .reportName("Finance Dashboard Report " + now.toLocalDate())
                .filters(filters)
                .retryCount(0)
                .generatedAtUtc(now)
                .isActive(true)
                .build();

        Report savedReport = reportRepository.save(reportJob);

        // TODO: Enqueue background worker task — ReportGenerationWorker will
        // poll `reports` WHERE fk_status_id = PENDING, query sales_aggregates
        // for total_gmv over the date range, calculate platform fee revenue,
        // join `payments` for successful/failed/refunded breakdown, join a
        // future commissions table (Week 7 dependency, not yet built) for
        // vendor commission deductions, include pending payouts from
        // vendor_payouts WHERE status = PENDING, and calculate
        // net_platform_revenue = platform_fee_revenue - (total_payouts + chargebacks).
        // No Commission entity/repository exists yet — deliberately not
        // scaffolded this session per the Day 4 scope decision.

        reportAuditLogService.logReportAction(currentUser, savedReport, AUDIT_ACTION_NAME, true);

        // ==========================================
        // 5. RESPONSE MAPPING
        // ==========================================
        // created_at_utc is DB-generated (insertable = false) — reload before
        // mapping per the project's Unified Temporal Alignment rule.
        Report reloadedReport = reportRepository.findById(savedReport.getPkReportId())
                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                        "Report job vanished immediately after save"));

        return ReportJobStatusDto.builder()
                .jobId(reloadedReport.getPkReportId())
                .status(pendingStatus.getStatusName())
                .createdAt(reloadedReport.getCreatedAtUtc())
                .build();
    }
}
