package com.productadda.dto.notifications;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminAnalyticsDto {

    private LocalDateTime dateFrom;

    private LocalDateTime dateTo;

    private DeliveryCountsDto summary;

    private Double deliveryRate;

    private Double failureRate;

    private Map<String, DeliveryCountsDto> byChannel;

    private Map<String, DeliveryCountsDto> byEvent;

    private RetryStatsDto retryStats;
}
