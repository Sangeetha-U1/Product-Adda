package com.productadda.dto.payment;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * NEW DTO : PaymentPollVerifyRequestDto
 * Gateway-agnostic polling/verification request for POST /api/payments/verify.
 * NAMING NOTE: A PaymentVerifyRequestDto already exists for the legacy
 * Razorpay Payment Links flow (razorpayPaymentId/razorpaySignature/etc).
 * This is a deliberately distinct class name/shape to avoid colliding
 * with it - see CHANGES_DAY2.md.
 * ================================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentPollVerifyRequestDto {

    @NotBlank(message = "Payment id is required")
    private String paymentId;

    // Optional cross-check per the execution plan
    private String orderId;
}
