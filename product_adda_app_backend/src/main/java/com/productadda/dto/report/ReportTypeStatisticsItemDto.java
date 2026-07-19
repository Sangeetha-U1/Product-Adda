package com.productadda.dto.report;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportTypeStatisticsItemDto {

    private String reportType;

    private Long count;

    private Long successCount;

    private Double successRatePct;

    // Null until ReportGenerationWorker exists and completedAtUtc is ever
    // populated for reports of this type
    private Double avgGenerationTimeMs;
}
