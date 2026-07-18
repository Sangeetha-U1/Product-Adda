package com.productadda.dto.report;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportHistoryItemDto {

    private UUID jobId;

    private String reportName;

    // Human-readable report type name, e.g. "SALES"
    private String reportType;

    // Human-readable status name, e.g. "PENDING", "SUCCESS", "FAILED"
    private String status;

    // Human-readable export format name, e.g. "PDF"
    private String format;

    private LocalDateTime createdAt;

    private LocalDateTime completedAt;

    private Long fileSizeBytes;
}
