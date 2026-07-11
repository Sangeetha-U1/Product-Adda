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
 * Description: Full-visibility, sanitized admin view of a single
 * order including customer identity, all line items, payment
 * status, delivery partner assignment, and invoice tracking.
 * Reuses existing OrderItemSummaryDto, OrderAddressSummaryDto,
 * OrderCouponSummaryDto, and OrderPaymentSummaryDto rather than
 * duplicating nested shapes already present in this package.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminOrderDetailDto {

    private UUID orderId;

    private String orderNumber;

    private String statusName;

    private UUID customerId;

    private String customerFullName;

    private String customerEmail;

    private BigDecimal subtotal;

    private BigDecimal couponDiscount;

    private BigDecimal shippingCost;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private LocalDateTime createdAtUtc;

    private List<OrderItemSummaryDto> items;

    private OrderAddressSummaryDto address;

    private OrderCouponSummaryDto coupon;

    private OrderPaymentSummaryDto payment;

    private UUID deliveryPartnerId;

    private String deliveryPartnerName;

    private UUID invoiceId;

    private String invoiceNumber;

    private BigDecimal invoiceAmount;

    private LocalDateTime invoiceGeneratedAt;

    // TODO: Add cancellationReason, cancelledBy, cancelledAtUtc fields once
    // Order.java is extended with cancellation tracking columns.
    // Depends on: a future cancellation_reasons lookup table plus the
    // OrderCancellationService business rules, scheduled for a later
    // Week 6 day per the execution plan. Field is included here as a
    // placeholder (always null for now) so the response shape does not
    // need to change again once that day lands.
    private String cancellationReason;
}
