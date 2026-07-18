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
public class ReportDownloadResponseDto {

    private UUID jobId;

    private String reportName;

    private String presignedUrl;

    private LocalDateTime urlExpirationUtc;
}
