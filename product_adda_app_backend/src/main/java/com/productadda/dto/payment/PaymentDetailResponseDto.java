package com.productadda.dto.payment;

import java.time.LocalDateTime;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/*
 * ================================================================
 * RESPONSE DTO
 * Description: Rich payment detail view for a single payment
 * belonging to the authenticated user. Excludes gatewaySignature,
 * raw metadata, and internal errorMessage per PCI-masking posture.
 * ================================================================
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentDetailResponseDto {

    private UUID paymentId;
    private UUID orderId;
    private String orderNumber;
    private String statusName;
    private String paymentMethodDisplay;
    private String gatewayName;
    private String gatewayTransactionId;
    private String gatewayOrderId;
    private String gatewayPaymentLinkId;
    private Long amountInPaise;
    private String currency;
    private LocalDateTime createdAtUtc;
    private LocalDateTime paidAtUtc;
    private LocalDateTime capturedAtUtc;
    private LocalDateTime failedAtUtc;
    private String failureReason;
}