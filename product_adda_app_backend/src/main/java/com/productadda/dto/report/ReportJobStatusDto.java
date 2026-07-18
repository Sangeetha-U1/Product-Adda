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
public class ReportJobStatusDto {

    private UUID jobId;

    // Human-readable status name, e.g. "PENDING"
    private String status;

    private LocalDateTime createdAt;
}
