package com.productadda.dto.notifications;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationHealthLiveResponseDto {

    private String workerName;

    private Boolean isHealthy;

    private LocalDateTime lastProcessedAtUtc;

    private Long processedCount;

    private Long secondsSinceLastRun;

    // count of notifications currently PENDING or
    // RETRYING (not yet concluded). Lets a caller distinguish "worker
    // alive but nothing to do" from "worker alive with a growing
    // backlog" without a separate call.
    private Long queueDepth;
}
