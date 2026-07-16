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
public class NotificationGateDecisionDto {

    // One of: "SEND", "SKIP", "DEFER". Kept as a plain String rather
    // than a Java enum per the project's zero-enum standard -- even
    // though this is a transient in-memory decision, never persisted.
    private String decisionType;

    private LocalDateTime deferUntilUtc;

    private String reason;
}
