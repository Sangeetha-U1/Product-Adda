package com.productadda.dto.product;

import lombok.*;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminModerationStatsResponseDto {
    private long totalPendingProducts;
    private long approvedToday;
    private long rejectedToday;
    private long pendingVendorsCount;
    private double averageApprovalTimeHours;
    private String lastUpdateUtc;
}