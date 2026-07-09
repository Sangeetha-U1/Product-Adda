package com.productadda.dto.order;

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
 * Description: Sanitized, frontend-ready summary of a single order
 * belonging to the authenticated customer. Excludes payment details
 * per Day 1 security requirements.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerOrderListDto {

    private UUID orderId;

    private String orderNumber;

    private String statusName;

    private BigDecimal totalAmount;

    private long itemCount;

    private LocalDateTime createdAtUtc;

    private String shippingCity;

    private String shippingState;
}
