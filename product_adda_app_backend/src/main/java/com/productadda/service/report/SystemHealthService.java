package com.productadda.service.report;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.report.MaterializedViewStatusResponseDto;
import com.productadda.dto.report.SystemHealthResponseDto;

import com.productadda.repository.ReportRepository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SystemHealthService {

    private static final long DB_DEGRADED_THRESHOLD_MS = 100;
    private static final long DB_CRITICAL_THRESHOLD_MS = 500;
    private static final long MATERIALIZED_VIEW_DEGRADED_THRESHOLD_MINUTES = 1440;
    private static final long MATERIALIZED_VIEW_CRITICAL_THRESHOLD_MINUTES = 2880;
    private static final int COMPONENT_SCORE_OK = 100;
    private static final int COMPONENT_SCORE_DEGRADED = 75;
    private static final int COMPONENT_SCORE_CRITICAL = 25;
    private static final String PENDING_STATUS_NAME = "PENDING";
    private static final String FAILED_STATUS_NAME = "FAILED";

    @PersistenceContext
    private final EntityManager entityManager;

    private final ReportRepository reportRepository;
    private final MaterializedViewStatusRetrievalService materializedViewStatusRetrievalService;

    /*
     * ================================================================
     * 1. VALIDATION SECTION (1.1 Request, 1.2 Auth/Context, 1.3 DB Lookup)
     * 1.1 Request: Not applicable - read-only GET endpoint, no request body.
     * 1.2 Context: Any authenticated user may call this endpoint (enforced
     * upstream by Spring Security since /api/reports/** is not listed in
     * PublicRoutes.java).
     * ================================================================
     * 2. BUSINESS RULES & PROCESSING / WORKFLOW
     * ================================================================
     * 3. DB SAVING SECTION (Skipped: read-only GET, no persistence occurs)
     * ================================================================
     * 4. POST-SAVING DATA SANITIZATION & MASKING (Repurposed for this
     * read-only endpoint: component status derivation and health-score
     * calculation happen here, since there is no DB save to sanitize
     * output from)
     * ================================================================
     * 5. RESPONSE MAPPING
     * ================================================================
     * Description: Reports live health only for components that actually
     * exist in this project (database, snapshot tables reused from
     * MaterializedViewStatusRetrievalService). Cache, S3, and the
     * background worker do not exist yet and are honestly reported as
     * NOT_CONFIGURED / NOT_IMPLEMENTED rather than simulated. The
     * composite health score is averaged only across implemented
     * components - see TODO below.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public SystemHealthResponseDto getSystemHealth() {

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // (native connectivity probe, mirrors HealthService.getDatabaseHealth)
        // ==========================================
        long dbStartTime = System.currentTimeMillis();
        String databaseStatus;
        long databaseLatencyMs;
        try {
            entityManager.createNativeQuery("SELECT 1").getSingleResult();
            databaseLatencyMs = System.currentTimeMillis() - dbStartTime;
            if (databaseLatencyMs >= DB_CRITICAL_THRESHOLD_MS) {
                databaseStatus = "CRITICAL";
            } else if (databaseLatencyMs >= DB_DEGRADED_THRESHOLD_MS) {
                databaseStatus = "DEGRADED";
            } else {
                databaseStatus = "OK";
            }
        } catch (Exception exception) {
            databaseLatencyMs = System.currentTimeMillis() - dbStartTime;
            databaseStatus = "CRITICAL";
        }

        // ==========================================
        // 2. BUSINESS RULES & PROCESSING
        // ==========================================
        // Worker component: ReportGenerationWorker does not exist, so
        // there is no heartbeat to check. Pending/failed job counts are
        // real, queried directly, and surfaced as informational metrics
        // even though the component itself is NOT_IMPLEMENTED.
        long workerPendingJobsCount = reportRepository.countByStatusName(PENDING_STATUS_NAME);
        LocalDateTime twentyFourHoursAgo = LocalDateTime.now(ZoneOffset.UTC).minusHours(24);
        long workerFailedJobsLast24hCount = reportRepository.countByStatusNameSince(FAILED_STATUS_NAME,
                twentyFourHoursAgo);
        String workerStatus = "NOT_IMPLEMENTED";

        String cacheStatus = "NOT_CONFIGURED";
        String s3Status = "NOT_CONFIGURED";

        // Reuses the existing freshness computation rather than duplicating
        // it - see MaterializedViewStatusRetrievalService.
        MaterializedViewStatusResponseDto materializedViewStatus = materializedViewStatusRetrievalService
                .getMaterializedViewStatus();
        Long viewFreshnessAgeMinutes = materializedViewStatus.getViewFreshnessAgeMinutes();
        String materializedViewsStatus;
        if (viewFreshnessAgeMinutes == null
                || viewFreshnessAgeMinutes >= MATERIALIZED_VIEW_CRITICAL_THRESHOLD_MINUTES) {
            materializedViewsStatus = "CRITICAL";
        } else if (viewFreshnessAgeMinutes >= MATERIALIZED_VIEW_DEGRADED_THRESHOLD_MINUTES) {
            materializedViewsStatus = "DEGRADED";
        } else {
            materializedViewsStatus = "OK";
        }

        // ==========================================
        // 4. POST-SAVING DATA SANITIZATION & MASKING
        // (composite health score, computed only across implemented
        // components - cache/s3/worker are excluded from the denominator
        // until they are actually built)
        // ==========================================
        // TODO: once Redis cache, S3, and ReportGenerationWorker are
        // implemented, include their component scores in this average.
        int databaseScore = scoreForStatus(databaseStatus);
        int materializedViewsScore = scoreForStatus(materializedViewsStatus);
        int healthScore = (databaseScore + materializedViewsScore) / 2;

        String overallStatus;
        if ("CRITICAL".equals(databaseStatus) || "CRITICAL".equals(materializedViewsStatus)) {
            overallStatus = "CRITICAL";
        } else if ("DEGRADED".equals(databaseStatus) || "DEGRADED".equals(materializedViewsStatus)) {
            overallStatus = "DEGRADED";
        } else {
            overallStatus = "HEALTHY";
        }

        // ==========================================
        // 5. RESPONSE MAPPING
        // ==========================================
        return SystemHealthResponseDto.builder()
                .overallStatus(overallStatus)
                .healthScore(healthScore)
                .databaseStatus(databaseStatus)
                .databaseLatencyMs(databaseLatencyMs)
                .cacheStatus(cacheStatus)
                .s3Status(s3Status)
                .workerStatus(workerStatus)
                .workerPendingJobsCount(workerPendingJobsCount)
                .workerFailedJobsLast24hCount(workerFailedJobsLast24hCount)
                .materializedViewsStatus(materializedViewsStatus)
                .materializedViewsFreshnessAgeMinutes(viewFreshnessAgeMinutes)
                .checkedAtUtc(LocalDateTime.now(ZoneOffset.UTC))
                .build();
    }

    private int scoreForStatus(String status) {
        if ("OK".equals(status)) {
            return COMPONENT_SCORE_OK;
        }
        if ("DEGRADED".equals(status)) {
            return COMPONENT_SCORE_DEGRADED;
        }
        return COMPONENT_SCORE_CRITICAL;
    }
}
