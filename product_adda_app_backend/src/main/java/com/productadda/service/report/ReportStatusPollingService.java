package com.productadda.service.report;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.report.ReportStatusResponseDto;
import com.productadda.entity.Report;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.exception.ApiException;
import com.productadda.repository.ReportRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportStatusPollingService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ReportRepository reportRepository;

    /*
     * ================================================================
     * 1. VALIDATION SECTION (1.1 Request, 1.2 Auth/Context, 1.3 DB Lookup)
     * ================================================================
     * 2. BUSINESS RULES & PROCESSING / WORKFLOW
     * ================================================================
     * 3. DB SAVING SECTION (Skipped: read-only polling operation)
     * ================================================================
     * 4. POST-SAVING DATA SANITIZATION & MASKING (Skipped: status payload
     * carries no PII/sensitive fields)
     * ================================================================
     * 5. RESPONSE MAPPING
     * ================================================================
     * Description: Returns the current processing status of a single report
     * job. Admins may poll any job; all other users may only poll jobs they
     * personally initiated (fk_user_id match).
     * ================================================================
     */
    @Transactional(readOnly = true)
    public ReportStatusResponseDto getReportStatus(UUID jobId) {

        // ==========================================
        // 1.1 REQUEST VALIDATION
        // ==========================================
        if (jobId == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "jobId is required");
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

        Report report = reportRepository.findById(jobId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "Report job not found"));

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
        boolean isAdmin = userRoles.stream()
                .map(role -> role.getFkRole().getRoleName())
                .anyMatch(name -> "ADMIN".equals(name) || "SUPER_ADMIN".equals(name));

        // Tenant isolation: non-admins may only poll jobs they personally
        // initiated.
        if (!isAdmin && !report.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to this report job");
        }

        // ==========================================
        // 2. BUSINESS RULES & PROCESSING
        // ==========================================
        // No branching logic required — this is a straight status readback.

        // ==========================================
        // 5. RESPONSE MAPPING
        // ==========================================
        return ReportStatusResponseDto.builder()
                .jobId(report.getPkReportId())
                .reportName(report.getReportName())
                .reportType(report.getFkReportType().getReportTypeName())
                .status(report.getFkStatus().getStatusName())
                .format(report.getFkExportFormat().getFormatName())
                .createdAt(report.getCreatedAtUtc())
                .completedAt(report.getCompletedAtUtc())
                .errorMessage(report.getErrorMessage())
                .build();
    }
}
