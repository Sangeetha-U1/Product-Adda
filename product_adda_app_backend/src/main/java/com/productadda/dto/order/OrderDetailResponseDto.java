package com.productadda.dto.order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized, frontend-ready complete order detail
 * including line items, address, coupon, and payment snapshots.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDetailResponseDto {

    private UUID orderId;

    private String orderNumber;

    private String statusName;

    private BigDecimal subtotal;

    private BigDecimal couponDiscount;

    private BigDecimal shippingCost;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private LocalDateTime createdAtUtc;

    private List<OrderItemSummaryDto> items;

    private OrderAddressSummaryDto address;

    // Null when the order did not use a coupon.
    private OrderCouponSummaryDto coupon;

    // Null when no payment has been initiated yet.
    private OrderPaymentSummaryDto payment;
}
