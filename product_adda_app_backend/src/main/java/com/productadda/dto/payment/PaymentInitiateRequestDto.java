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
public class PaymentInitiateRequestDto {

    @NotBlank(message = "Order id is required")
    private String orderId;

    @NotBlank(message = "Payment gateway is required")
    private String paymentGateway;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    // Client-generated UUIDv4 string, enforces duplicate-charge protection server-side
    @NotBlank(message = "Idempotency key is required")
    private String idempotencyKey;

    // Optional: reference to a previously saved/tokenized payment method
    private String savedPaymentMethodId;

    private String deviceFingerprint;

    private String ipAddress;

    private String userAgent;

    // Note: amount_in_paise is intentionally NOT accepted from the client.
    // PaymentServicePaymentInitiate re-derives it server-side from
    // orders.total_amount to prevent client-side amount tampering.
}
