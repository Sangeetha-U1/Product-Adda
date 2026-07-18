package com.productadda.service.report;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.report.InventoryAnalyticsReportRequestDto;
import com.productadda.dto.report.ReportJobStatusDto;
import com.productadda.entity.Report;
import com.productadda.entity.ReportExportFormat;
import com.productadda.entity.ReportJobStatus;
import com.productadda.entity.ReportType;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.entity.Vendor;
import com.productadda.exception.ApiException;
import com.productadda.repository.ReportExportFormatRepository;
import com.productadda.repository.ReportJobStatusRepository;
import com.productadda.repository.ReportRepository;
import com.productadda.repository.ReportTypeRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.repository.VendorRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class InventoryAnalyticsReportInitiationService {

        private static final String REPORT_TYPE_NAME = "INVENTORY_ANALYTICS";
        private static final String PENDING_STATUS_NAME = "PENDING";
        private static final String AUDIT_ACTION_NAME = "REPORT_GENERATED";

        private final UserRepository userRepository;
        private final UserRoleRepository userRoleRepository;
        private final VendorRepository vendorRepository;
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
         * Description: Creates a PENDING async job record for an inventory
         * analytics report. No date range applies — this report reflects
         * current stock/velocity state, not a historical window. Vendors are
         * hard-scoped server-side to their own vendor id.
         * ================================================================
         */
        @Transactional
        public ReportJobStatusDto initiateInventoryAnalyticsReport(InventoryAnalyticsReportRequestDto request) {

                // ==========================================
                // 1.1 REQUEST VALIDATION
                // ==========================================
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
                boolean isAdmin = userRoles.stream()
                                .map(role -> role.getFkRole().getRoleName())
                                .anyMatch(name -> "ADMIN".equals(name) || "SUPER_ADMIN".equals(name));
                boolean isVendor = userRoles.stream()
                                .map(role -> role.getFkRole().getRoleName())
                                .anyMatch("VENDOR"::equals);
                if (!isAdmin && !isVendor) {
                        throw new ApiException(HttpStatus.FORBIDDEN,
                                        "Only admins and vendors can generate inventory analytics reports");
                }

                // Vendor-branch resolution: hard tenant isolation, vendor id is
                // always resolved server-side, never trusted from the request body.
                Vendor authenticatedVendor = null;
                final String ownVendorId;

                if (isVendor) {
                        authenticatedVendor = vendorRepository.findByFkUser(currentUser)
                                        .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND,
                                                        "No associated vendor profile found for this user account"));

                        ownVendorId = authenticatedVendor.getPkVendorId().toString();

                        if (request.getVendorIds() != null) {
                                boolean requestsOtherVendor = request.getVendorIds().stream()
                                                .anyMatch(vendorId -> vendorId != null
                                                                && !ownVendorId.equalsIgnoreCase(vendorId));
                                if (requestsOtherVendor) {
                                        throw new ApiException(HttpStatus.FORBIDDEN,
                                                        "Vendor cannot access data for other vendors");
                                }
                        }
                } else {
                        ownVendorId = null;
                }

                ReportType reportType = reportTypeRepository.findByReportTypeName(REPORT_TYPE_NAME)
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "INVENTORY_ANALYTICS report type not configured"));

                ReportJobStatus pendingStatus = reportJobStatusRepository.findByStatusName(PENDING_STATUS_NAME)
                                .orElseThrow(() -> new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                                                "PENDING job status not configured"));

                ReportExportFormat exportFormat = reportExportFormatRepository.findByFormatName(request.getFormat())
                                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST,
                                                "Format must be PDF, CSV, or JSON"));

                // ==========================================
                // 2. BUSINESS RULES & PROCESSING
                // ==========================================
                LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

                Map<String, Object> filters = new HashMap<>();
                if (isVendor) {
                        filters.put("vendorId", ownVendorId);
                } else if (request.getVendorIds() != null) {
                        filters.put("vendorIds", request.getVendorIds());
                }
                if (request.getCategoryIds() != null) {
                        filters.put("categoryIds", request.getCategoryIds());
                }

                // ==========================================
                // 3. DB SAVING SECTION
                // ==========================================
                Report reportJob = Report.builder()
                                .pkReportId(uuidUtil.generateUuidV7())
                                .fkUser(currentUser)
                                .fkReportType(reportType)
                                .fkVendor(isVendor ? authenticatedVendor : null)
                                .fkStatus(pendingStatus)
                                .fkExportFormat(exportFormat)
                                .reportName("Inventory Analytics Report " + now.toLocalDate())
                                .filters(filters)
                                .retryCount(0)
                                .generatedAtUtc(now)
                                .isActive(true)
                                .build();

                Report savedReport = reportRepository.save(reportJob);

                // TODO: Enqueue background worker task — ReportGenerationWorker
                // will poll `reports` WHERE fk_status_id = PENDING, query
                // inventory_aggregates scoped by vendor_id (if vendor) or all (if
                // admin), calculate velocity_score, flag reorder candidates where
                // current_stock is below reorder_point, and identify fast-moving
                // products above the 75th percentile velocity.

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
