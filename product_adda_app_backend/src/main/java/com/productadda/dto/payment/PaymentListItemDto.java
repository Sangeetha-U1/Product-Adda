package com.productadda.dto.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * NESTED ITEM DTO
 * Description: Minimal, list-view payment summary for the
 * authenticated user's payment history.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentListItemDto {

    private UUID paymentId;
    private UUID orderId;
    private String orderNumber;
    private String statusName;
    private String paymentMethod;
    private String gatewayName;
    private Long amountInPaise;
    private String currency;
    private LocalDateTime createdAtUtc;
    private LocalDateTime paidAtUtc;
}