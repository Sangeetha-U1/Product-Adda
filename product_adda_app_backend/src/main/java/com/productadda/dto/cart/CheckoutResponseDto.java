package com.productadda.dto.cart;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import com.productadda.dto.order.OrderItemSummaryDto;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Sanitized order confirmation payload returned
 * immediately after a successful (or idempotently replayed)
 * checkout transaction.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CheckoutResponseDto {

    private UUID orderId;

    private String orderNumber;

    private String statusName;

    private BigDecimal subtotal;

    private BigDecimal couponDiscount;

    private BigDecimal shippingCost;

    private BigDecimal taxAmount;

    private BigDecimal totalAmount;

    private UUID idempotencyKey;

    private LocalDateTime createdAtUtc;

    private List<OrderItemSummaryDto> items;
}
