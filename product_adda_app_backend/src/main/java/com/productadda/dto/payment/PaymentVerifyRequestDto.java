package com.productadda.dto.payment;

import jakarta.validation.constraints.NotBlank;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentVerifyRequestDto {

    @NotBlank(message = "Razorpay payment id is required")
    private String razorpayPaymentId;

    @NotBlank(message = "Razorpay payment link id is required")
    private String razorpayPaymentLinkId;

    @NotBlank(message = "Razorpay order id is required")
    private String razorpayPaymentLinkReferenceId;

    @NotBlank(message = "Razorpay payment link is required")
    private String razorpayPaymentLinkStatus;

    @NotBlank(message = "Razorpay signature is required")
    private String razorpaySignature;
}