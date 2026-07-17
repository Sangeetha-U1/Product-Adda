package com.productadda.dto.notifications;

import java.util.List;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadinessResponseDto {

    // "ready" or "not_ready"
    private String status;

    // Per-component status: database/email -> healthy|unhealthy,
    // sms/push -> inactive (always), in_app -> healthy (always, no
    // external dependency), queue_worker -> alive|stuck
    private Map<String, String> componentStatus;

    // Empty when status = "ready". "inactive" components are never
    // listed here -- inactive is an expected, passing state, not a
    // failure (per the plan's own Test Scenario 9).
    private List<String> failingComponents;
}
