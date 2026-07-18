package com.productadda.service.report;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.report.ReportDownloadResponseDto;
import com.productadda.entity.Report;
import com.productadda.entity.ReportPresignedUrl;
import com.productadda.entity.User;
import com.productadda.entity.UserRole;
import com.productadda.exception.ApiException;
import com.productadda.repository.ReportPresignedUrlRepository;
import com.productadda.repository.ReportRepository;
import com.productadda.repository.UserRepository;
import com.productadda.repository.UserRoleRepository;
import com.productadda.util.UuidUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportDownloadService {

    private static final String SUCCESS_STATUS_NAME = "SUCCESS";

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final ReportRepository reportRepository;
    private final ReportPresignedUrlRepository reportPresignedUrlRepository;
    private final ReportStorageService reportStorageService;
    private final UuidUtil uuidUtil;

    /*
     * ================================================================
     * 1. VALIDATION SECTION (1.1 Request, 1.2 Auth/Context, 1.3 DB Lookup)
     * ================================================================
     * 2. ANTI-SPAM CACHE EVALUATION WORKFLOW
     * ================================================================
     * 3. DB SAVING SECTION (cache-miss signing path only)
     * ================================================================
     * 4. POST-SAVING DATA SANITIZATION & MASKING (Skipped: presigned URL
     * response carries no PII/sensitive fields beyond the signed link
     * itself, which is the intended payload)
     * ================================================================
     * 5. RESPONSE MAPPING
     * ================================================================
     * Description: Secures a short-lived Backblaze B2 download link for a
     * completed report file, following the same anti-spam DB-cache pattern
     * as InvoiceDownloadService. Only reports in SUCCESS status are
     * downloadable — anything else returns 409 Conflict.
     * ================================================================
     */
    @Transactional
    public ReportDownloadResponseDto getReportDownloadUrl(UUID jobId) {

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

        // Soft-delete guard: block downloads targeting disabled records.
        if (!Boolean.TRUE.equals(report.getIsActive())) {
            throw new ApiException(HttpStatus.GONE, "Target report asset context is disabled");
        }

        List<UserRole> userRoles = userRoleRepository.findByFkUser(currentUser);
        boolean isAdmin = userRoles.stream()
                .map(role -> role.getFkRole().getRoleName())
                .anyMatch(name -> "ADMIN".equals(name) || "SUPER_ADMIN".equals(name));

        // Tenant isolation: non-admins may only download reports they
        // personally initiated.
        if (!isAdmin && !report.getFkUser().getPkUserId().equals(currentUser.getPkUserId())) {
            throw new ApiException(HttpStatus.FORBIDDEN, "Access denied to download this report");
        }

        // A report is only downloadable once its background job has reached
        // SUCCESS — PENDING/PROCESSING/FAILED are all "not ready".
        if (!SUCCESS_STATUS_NAME.equals(report.getFkStatus().getStatusName())) {
            throw new ApiException(HttpStatus.CONFLICT,
                    "Report not ready for download: current status is " + report.getFkStatus().getStatusName());
        }

        if (report.getFileUrl() == null || report.getFileUrl().trim().isEmpty()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Report marked SUCCESS but has no stored file reference");
        }

        // ==========================================
        // 2. ANTI-SPAM CACHE EVALUATION WORKFLOW
        // ==========================================
        LocalDateTime nowUtc = LocalDateTime.now(ZoneOffset.UTC);

        Optional<ReportPresignedUrl> cachedRecordOpt = reportPresignedUrlRepository
                .findByFkReport_PkReportIdAndIsActiveTrue(jobId);

        if (cachedRecordOpt.isPresent()) {
            ReportPresignedUrl cachedRecord = cachedRecordOpt.get();

            // Evaluate if cached token is still alive and safe for re-use
            if (cachedRecord.getExpiresAtUtc().isAfter(nowUtc)) {
                return ReportDownloadResponseDto.builder()
                        .jobId(report.getPkReportId())
                        .reportName(report.getReportName())
                        .presignedUrl(cachedRecord.getPresignedUrl())
                        .urlExpirationUtc(cachedRecord.getExpiresAtUtc())
                        .build();
            } else {
                // Token is expired -> Soft delete by turning active flag off
                cachedRecord.setIsActive(false);
                cachedRecord.setUpdatedAtUtc(nowUtc);
                reportPresignedUrlRepository.save(cachedRecord);
            }
        }

        // ==========================================
        // 3. CACHE MISS / SIGNING GENERATION WORKFLOW
        // ==========================================
        String securedObjectKey = report.getFileUrl();
        Duration validityWindow = Duration.ofMinutes(15);

        String transientUrl = reportStorageService.generatePresignedUrl(securedObjectKey, validityWindow);

        if (transientUrl == null || transientUrl.trim().isEmpty()) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to secure transient cloud access credential tokens");
        }

        LocalDateTime freshExpirationUtc = nowUtc.plusMinutes(15);

        // Commit active tracking footprint record to avoid calculation spamming
        ReportPresignedUrl newCacheEntry = ReportPresignedUrl.builder()
                .pkPresignedUrlId(uuidUtil.generateUuidV7())
                .fkReport(report)
                .presignedUrl(transientUrl)
                .expiresAtUtc(freshExpirationUtc)
                .isActive(true)
                .createdAtUtc(nowUtc)
                .updatedAtUtc(nowUtc)
                .build();

        reportPresignedUrlRepository.save(newCacheEntry);

        // ==========================================
        // 5. RESPONSE MAPPING
        // ==========================================
        return ReportDownloadResponseDto.builder()
                .jobId(report.getPkReportId())
                .reportName(report.getReportName())
                .presignedUrl(transientUrl)
                .urlExpirationUtc(freshExpirationUtc)
                .build();
    }
}
