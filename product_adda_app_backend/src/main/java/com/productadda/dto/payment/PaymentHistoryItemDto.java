package com.productadda.dto.payment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * NESTED ITEM DTO: PaymentHistoryItemDto
 * Sanitized, frontend-ready summary of a single payment belonging
 * to the authenticated customer. Mirrors CustomerOrderListDto's
 * style exactly. PCI-masked payment method display only.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentHistoryItemDto {

    private String paymentId;

    private String orderId;

    private String orderNumber;

    private String status;

    private Long amountInPaise;

    // PCI-masked display value, e.g. "Card ending in 4242"
    private String paymentMethodDisplay;

    private String paymentGateway;

    private LocalDateTime createdAtUtc;

    private LocalDateTime capturedAtUtc;

    private Long refundedAmountInPaise;
}
