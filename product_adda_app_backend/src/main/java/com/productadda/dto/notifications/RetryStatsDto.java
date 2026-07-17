package com.productadda.dto.notifications;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetryStatsDto {

    private long totalRetried;

    private long successfulAfterRetry;

    private long maxRetriesFailed;
}
