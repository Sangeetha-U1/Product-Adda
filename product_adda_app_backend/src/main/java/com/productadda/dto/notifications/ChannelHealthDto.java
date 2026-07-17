package com.productadda.dto.notifications;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelHealthDto {

    // "active" (at least one attempt in the last 24h) or "inactive"
    // (zero attempts -- matches SMS/PUSH today, since no rows are ever
    // created for those channels yet per the Day 1 channel-scope
    // decision). successRate/failureRate/errorBreakdown are null when
    // inactive.
    private String status;

    private Double successRate;

    private Double failureRate;

    private Map<String, Long> errorBreakdown;
}
