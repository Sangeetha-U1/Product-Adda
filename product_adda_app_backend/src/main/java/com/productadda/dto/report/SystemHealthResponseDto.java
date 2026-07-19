package com.productadda.dto.report;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthResponseDto {

    // OK / DEGRADED / CRITICAL, derived only from implemented components
    // (database, materializedViews) - see SystemHealthService
    private String overallStatus;

    // 0-100, averaged only across implemented components today
    private Integer healthScore;

    private String databaseStatus;

    private Long databaseLatencyMs;

    // TODO: always NOT_CONFIGURED - no Redis/cache layer exists in this
    // project yet
    private String cacheStatus;

    // TODO: always NOT_CONFIGURED - no S3 client exists in this project yet
    // (BackblazeConfig is unrelated file storage, not S3)
    private String s3Status;

    // TODO: always NOT_IMPLEMENTED - ReportGenerationWorker does not exist
    private String workerStatus;

    private Long workerPendingJobsCount;

    private Long workerFailedJobsLast24hCount;

    private String materializedViewsStatus;

    private Long materializedViewsFreshnessAgeMinutes;

    private LocalDateTime checkedAtUtc;
}
