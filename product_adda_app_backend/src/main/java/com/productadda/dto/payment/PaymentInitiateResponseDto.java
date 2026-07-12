package com.productadda.dto.payment;

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
public class PaymentInitiateResponseDto {

    private String paymentId;

    private String orderId;

    // Human-readable status name, e.g. "PENDING"
    private String status;

    private Long amountInPaise;

    private String currency;

    private String paymentGateway;

    // Gateway-assigned order id (Razorpay order_id)
    private String gatewayOrderId;

    // Frontend redirects the browser here to complete checkout, if applicable (Razorpay). Null for COD.
    private String checkoutRedirectUrl;

    // Public/checkout key the frontend SDK needs (Razorpay key_id)
    private String checkoutKey;
}
