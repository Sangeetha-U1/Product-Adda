package com.productadda.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/*
 * ================================================================
 * MIDDLE-MAN DTO: RazorpayWebhookPayloadDto
 * Internal transfer object for the parsed Razorpay webhook body.
 * Not REST-facing. No validation annotations: HMAC signature
 * verification against the raw body is the trust boundary here,
 * performed before this DTO is even parsed.
 * ================================================================
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RazorpayWebhookPayloadDto {

    // e.g. "payment.authorized", "payment.failed", "payment.captured"
    private String eventType;

    // Razorpay's own webhook delivery id, used for idempotency de-duplication
    private String webhookId;

    // Razorpay order id / payment entity id, maps to payments.gateway_transaction_id
    private String gatewayEntityId;

    // Raw gateway status string, e.g. "captured", "authorized", "failed"
    private String gatewayStatus;

    private Long amountInPaise;

    // Present only on payment.failed events
    private String failureReason;

    // Full raw JSON body, retained for the audit log's details column
    private String rawPayloadJson;
}
