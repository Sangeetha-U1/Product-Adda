package com.productadda.dto.customer;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * NESTED ITEM DTO
 * Description: Sanitized summary of a single DELIVERED order
 * belonging to the authenticated customer, including the full
 * delivery timestamp trail. Excludes payment details, consistent
 * with CustomerOrderListDto.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDeliveryHistoryListDto {

    private UUID orderId;
    private String orderNumber;
    private String statusName;
    private BigDecimal totalAmount;
    private long itemCount;
    private LocalDateTime orderedAtUtc;
    private LocalDateTime pickedUpAtUtc;
    private LocalDateTime outForDeliveryAtUtc;
    private LocalDateTime deliveredAtUtc;
    private String shippingCity;
    private String shippingState;
}
