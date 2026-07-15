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
}
