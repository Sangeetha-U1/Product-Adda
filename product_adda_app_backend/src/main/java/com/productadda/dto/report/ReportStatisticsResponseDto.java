package com.productadda.dto.report;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportStatisticsResponseDto {

    private Long totalToday;

    private Long successCount;

    private Long failureCount;

    // Null when totalToday is 0
    private Double successRatePct;

    // Null today - no report has ever reached SUCCESS with a populated
    // completedAtUtc, since ReportGenerationWorker does not exist yet
    private Double avgGenerationTimeMs;

    private Double p50GenerationTimeMs;

    private Double p95GenerationTimeMs;

    private List<ReportTypeStatisticsItemDto> byReportType;

    private Integer maxJobsPerHour;

    private Double currentLoadPct;

    // Populated only when totalToday > 0 and successRatePct falls below the
    // warning threshold defined in ReportStatisticsService
    private String healthWarning;

    private LocalDateTime checkedAtUtc;
}
