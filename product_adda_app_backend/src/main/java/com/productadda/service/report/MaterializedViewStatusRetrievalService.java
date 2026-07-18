package com.productadda.service.report;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.productadda.dto.report.MaterializedViewStatusResponseDto;
import com.productadda.entity.InventoryAggregate;
import com.productadda.entity.SalesAggregate;
import com.productadda.exception.ApiException;
import com.productadda.repository.InventoryAggregateRepository;
import com.productadda.repository.SalesAggregateRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MaterializedViewStatusRetrievalService {

    private static final long FRESHNESS_THRESHOLD_MINUTES = 1440;
    private static final int REFRESH_HOUR_UTC = 2;

    private final SalesAggregateRepository salesAggregateRepository;
    private final InventoryAggregateRepository inventoryAggregateRepository;

    /*
     * ================================================================
     * 1. VALIDATION SECTION (1.1 Request, 1.2 Auth/Context, 1.3 DB Lookup)
     * 1.1 Request: Not applicable — read-only GET endpoint, no request body.
     * ================================================================
     * 2. BUSINESS RULES & PROCESSING / WORKFLOW
     * ================================================================
     * 3. DB SAVING SECTION (Skipped: read-only GET, no persistence occurs)
     * ================================================================
     * 4. POST-SAVING DATA SANITIZATION & MASKING (Repurposed for this
     * read-only endpoint: freshness/staleness derivation and null-safety
     * defaults are computed here from the fetched aggregate rows, since
     * there is no DB save to sanitize output from)
     * ================================================================
     * 5. RESPONSE MAPPING
     * ================================================================
     * Description: Reports snapshot-table freshness by reading directly
     * from sales_aggregates and inventory_aggregates (MAX(updated_at_utc)
     * and row counts). MySQL has no true materialized views, and no
     * MaterializedViewScheduler/refresh-job exists yet to log a `reports`
     * row for this — this endpoint reflects live table state only. Any
     * authenticated user may call this endpoint; there is no role branch.
     * ================================================================
     */
    @Transactional(readOnly = true)
    public MaterializedViewStatusResponseDto getMaterializedViewStatus() {

        // ==========================================
        // 1.2 CONTEXT AUTHENTICATION
        // ==========================================
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Authentication missing or invalid");
        }

        // ==========================================
        // 1.3 DATABASE LOOKUP VALIDATION
        // ==========================================
        Optional<SalesAggregate> latestSalesAggregate = salesAggregateRepository.findTopByOrderByUpdatedAtUtcDesc();
        Optional<InventoryAggregate> latestInventoryAggregate = inventoryAggregateRepository
                .findTopByOrderByUpdatedAtUtcDesc();
        long salesAggregatesRowCount = salesAggregateRepository.count();
        long inventoryAggregatesRowCount = inventoryAggregateRepository.count();

        // ==========================================
        // 2. BUSINESS RULES & PROCESSING
        // ==========================================
        // No role-based branching applies here — every authenticated caller
        // receives an identical view of snapshot-table freshness.
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);

        // ==========================================
        // 4. POST-SAVING DATA SANITIZATION & MASKING
        // (freshness derivation, no-data defaults)
        // ==========================================
        LocalDateTime salesUpdatedAt = latestSalesAggregate.map(SalesAggregate::getUpdatedAtUtc).orElse(null);
        LocalDateTime inventoryUpdatedAt = latestInventoryAggregate.map(InventoryAggregate::getUpdatedAtUtc)
                .orElse(null);

        LocalDateTime lastRefreshAt = null;
        if (salesUpdatedAt != null && inventoryUpdatedAt != null) {
            lastRefreshAt = salesUpdatedAt.isAfter(inventoryUpdatedAt) ? salesUpdatedAt : inventoryUpdatedAt;
        } else if (salesUpdatedAt != null) {
            lastRefreshAt = salesUpdatedAt;
        } else if (inventoryUpdatedAt != null) {
            lastRefreshAt = inventoryUpdatedAt;
        }

        Long viewFreshnessAgeMinutes = lastRefreshAt == null
                ? null
                : ChronoUnit.MINUTES.between(lastRefreshAt, now);

        String freshnessStatus = (viewFreshnessAgeMinutes == null
                || viewFreshnessAgeMinutes >= FRESHNESS_THRESHOLD_MINUTES)
                        ? "STALE"
                        : "FRESH";

        // Next scheduled refresh is always the next occurrence of 2 AM UTC —
        // computed here rather than stored, since no scheduler persists it.
        LocalDateTime todayRefreshTime = LocalDateTime.of(now.toLocalDate(), LocalTime.of(REFRESH_HOUR_UTC, 0));
        LocalDateTime nextRefreshScheduledAt = now.isBefore(todayRefreshTime)
                ? todayRefreshTime
                : LocalDateTime.of(now.toLocalDate().plusDays(1), LocalTime.of(REFRESH_HOUR_UTC, 0));

        // TODO: lastRefreshDurationMs cannot be populated until
        // MaterializedViewScheduler exists and logs its own run duration —
        // always null for Day 4.

        // ==========================================
        // 5. RESPONSE MAPPING
        // ==========================================
        return MaterializedViewStatusResponseDto.builder()
                .lastRefreshAt(lastRefreshAt)
                .lastRefreshDurationMs(null)
                .nextRefreshScheduledAt(nextRefreshScheduledAt)
                .viewFreshnessAgeMinutes(viewFreshnessAgeMinutes)
                .status(freshnessStatus)
                .salesAggregatesRowCount(salesAggregatesRowCount)
                .inventoryAggregatesRowCount(inventoryAggregatesRowCount)
                .build();
    }
}
