package com.productadda.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * NEW DTO : PaymentPollVerifyResponseDto
 * Response for POST /api/payments/verify (gateway-agnostic polling).
 * ================================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPollVerifyResponseDto {

    private Boolean success;

    private String paymentId;

    private String orderId;

    // e.g. "SUCCESS", "FAILED", "PENDING"
    private String status;

    private String failureReason;

    private String message;
}
