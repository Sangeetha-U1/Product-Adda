package com.productadda.dto.notifications;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VendorAnalyticsDto {

    private UUID vendorId;

    private LocalDateTime dateFrom;

    private LocalDateTime dateTo;

    private Map<String, DeliveryCountsDto> byEvent;
}
