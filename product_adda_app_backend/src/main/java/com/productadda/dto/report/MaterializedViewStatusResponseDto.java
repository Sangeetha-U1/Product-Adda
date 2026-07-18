package com.productadda.dto.report;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * =========================================================
 * MATERIALIZED VIEW / SNAPSHOT TABLE STATUS RESPONSE
 * Description: Reports freshness of the sales_aggregates and
 * inventory_aggregates snapshot tables. MySQL has no true materialized
 * views, so this reflects direct reads off those two tables rather than a
 * `reports` row — no MaterializedViewScheduler/refresh-job exists yet to
 * produce one.
 * =========================================================
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MaterializedViewStatusResponseDto {

    private LocalDateTime lastRefreshAt;

    // TODO: always null for Day 4 — no scheduler exists yet to record and
    // report its own run duration. Populate once MaterializedViewScheduler
    // is built in a future week.
    private Long lastRefreshDurationMs;

    private LocalDateTime nextRefreshScheduledAt;

    private Long viewFreshnessAgeMinutes;

    private String status;

    private Long salesAggregatesRowCount;

    private Long inventoryAggregatesRowCount;
}
