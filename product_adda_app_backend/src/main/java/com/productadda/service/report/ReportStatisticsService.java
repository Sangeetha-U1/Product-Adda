package com.productadda.service.report;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.report.ReportStatisticsResponseDto;
import com.productadda.dto.report.ReportTypeStatisticsItemDto;

import com.productadda.entity.Report;

import com.productadda.repository.ReportRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReportStatisticsService {

    private static final int MAX_JOBS_PER_HOUR = 100;
    private static final double FAILURE_RATE_WARNING_THRESHOLD_PCT = 80.0;
    private static final String SUCCESS_STATUS_NAME = "SUCCESS";
    private static final String FAILED_STATUS_NAME = "FAILED";

    private final ReportRepository reportRepository;

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
     * read-only endpoint: aggregation, percentile, and null-safety
     * defaults are computed here from the fetched report rows)
     * ================================================================
     * 5. RESPONSE MAPPING
     * ================================================================
     * Description: Computes today's report generation statistics directly
     * from the reports table. Generation-timing metrics (avg/p50/p95) are
     * derived only from reports with a populated completedAtUtc - since
     * ReportGenerationWorker does not exist yet, no report has ever
     * reached SUCCESS with a completion timestamp, so these fields will
     * legitimately return null today rather than a fabricated value.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public ReportStatisticsResponseDto getReportStatistics() {

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime startOfTodayUtc = now.toLocalDate().atStartOfDay();
        LocalDateTime oneHourAgo = now.minusHours(1);

        List<Report> todaysReports = reportRepository.findByCreatedAtUtcGreaterThanEqual(startOfTodayUtc);
        long jobsInLastHour = reportRepository.countByCreatedAtUtcGreaterThanEqual(oneHourAgo);

        // ==========================================
        // 2. BUSINESS RULES & PROCESSING
        // ==========================================
        long totalToday = todaysReports.size();
        long successCount = todaysReports.stream()
                .filter(report -> SUCCESS_STATUS_NAME.equals(report.getFkStatus().getStatusName()))
                .count();
        long failureCount = todaysReports.stream()
                .filter(report -> FAILED_STATUS_NAME.equals(report.getFkStatus().getStatusName()))
                .count();

        List<Long> completedDurationsMs = new ArrayList<>();
        for (Report report : todaysReports) {
            if (report.getCompletedAtUtc() != null) {
                completedDurationsMs
                        .add(ChronoUnit.MILLIS.between(report.getCreatedAtUtc(), report.getCompletedAtUtc()));
            }
        }
        Collections.sort(completedDurationsMs);

        Map<String, List<Report>> reportsByType = new HashMap<>();
        for (Report report : todaysReports) {
            String reportTypeName = report.getFkReportType().getReportTypeName();
            reportsByType.computeIfAbsent(reportTypeName, key -> new ArrayList<>()).add(report);
        }

        // ==========================================
        // 4. POST-SAVING DATA SANITIZATION & MASKING
        // (aggregation, percentile calculation, null-safety defaults)
        // ==========================================
        Double successRatePct = totalToday == 0 ? null : (successCount * 100.0) / totalToday;
        Double avgGenerationTimeMs = averageOf(completedDurationsMs);
        Double p50GenerationTimeMs = percentileOf(completedDurationsMs, 0.50);
        Double p95GenerationTimeMs = percentileOf(completedDurationsMs, 0.95);
        Double currentLoadPct = (jobsInLastHour * 100.0) / MAX_JOBS_PER_HOUR;

        String healthWarning = null;
        if (totalToday > 0 && successRatePct != null && successRatePct < FAILURE_RATE_WARNING_THRESHOLD_PCT) {
            healthWarning = "High failure rate detected";
        }

        List<ReportTypeStatisticsItemDto> byReportType = new ArrayList<>();
        for (Map.Entry<String, List<Report>> entry : reportsByType.entrySet()) {
            List<Report> typeReports = entry.getValue();
            long typeCount = typeReports.size();
            long typeSuccessCount = typeReports.stream()
                    .filter(report -> SUCCESS_STATUS_NAME.equals(report.getFkStatus().getStatusName()))
                    .count();
            List<Long> typeDurationsMs = new ArrayList<>();
            for (Report report : typeReports) {
                if (report.getCompletedAtUtc() != null) {
                    typeDurationsMs
                            .add(ChronoUnit.MILLIS.between(report.getCreatedAtUtc(), report.getCompletedAtUtc()));
                }
            }
            byReportType.add(ReportTypeStatisticsItemDto.builder()
                    .reportType(entry.getKey())
                    .count(typeCount)
                    .successCount(typeSuccessCount)
                    .successRatePct(typeCount == 0 ? null : (typeSuccessCount * 100.0) / typeCount)
                    .avgGenerationTimeMs(averageOf(typeDurationsMs))
                    .build());
        }

        // ==========================================
        // 5. RESPONSE MAPPING
        // ==========================================
        return ReportStatisticsResponseDto.builder()
                .totalToday(totalToday)
                .successCount(successCount)
                .failureCount(failureCount)
                .successRatePct(successRatePct)
                .avgGenerationTimeMs(avgGenerationTimeMs)
                .p50GenerationTimeMs(p50GenerationTimeMs)
                .p95GenerationTimeMs(p95GenerationTimeMs)
                .byReportType(byReportType)
                .maxJobsPerHour(MAX_JOBS_PER_HOUR)
                .currentLoadPct(currentLoadPct)
                .healthWarning(healthWarning)
                .checkedAtUtc(now)
                .build();
    }

    // Computes a simple arithmetic mean; returns null on an empty input
    // rather than fabricating a zero value.
    private Double averageOf(List<Long> durationsMs) {
        if (durationsMs.isEmpty()) {
            return null;
        }
        long sum = 0;
        for (Long duration : durationsMs) {
            sum += duration;
        }
        return sum / (double) durationsMs.size();
    }

    // Nearest-rank percentile over an already-sorted ascending list;
    // returns null on an empty input rather than fabricating a value.
    private Double percentileOf(List<Long> sortedDurationsMs, double percentile) {
        if (sortedDurationsMs.isEmpty()) {
            return null;
        }
        int rank = (int) Math.ceil(percentile * sortedDurationsMs.size());
        int index = Math.max(0, Math.min(sortedDurationsMs.size() - 1, rank - 1));
        return sortedDurationsMs.get(index).doubleValue();
    }
}
