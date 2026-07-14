package com.productadda.dto.payment;

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
public class ReconciliationResultDto {

    private String reconciliationLogId;

    private String reconciliationRunId;

    // "completed" or "completed_with_discrepancies"
    private String status;

    private Integer totalGatewayTransactions;

    private Integer totalDbRecords;

    private Integer matchedRecords;

    private List<ReconciliationDiscrepancyDto> missingInDb;

    private List<ReconciliationDiscrepancyDto> orphanedInDb;

    private LocalDateTime reconciledAtUtc;

    private String message;
}
