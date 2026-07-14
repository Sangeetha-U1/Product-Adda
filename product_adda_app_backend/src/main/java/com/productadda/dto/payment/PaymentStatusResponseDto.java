package com.productadda.dto.payment;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * NEW DTO : PaymentStatusResponseDto
 * Sanitized, frontend-ready single-payment payload for
 * GET /api/payments/{paymentId}. PCI-masked: never includes full
 * card numbers, CVV, or expiry - only a masked display string.
 * ================================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentStatusResponseDto {

    private String paymentId;

    private String orderId;

    private String status;

    private Long amountInPaise;

    private String currency;

    // PCI-masked display value, e.g. "Card ending in 4242" - never the raw
    // payment_method column
    private String paymentMethodDisplay;

    private String paymentGateway;

    private LocalDateTime createdAtUtc;

    private LocalDateTime capturedAtUtc;

    // Only populated when status = FAILED
    private String failureReason;
}
