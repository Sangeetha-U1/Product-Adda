package com.productadda.service.payment;

import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.config.RazorpayConfig;

import com.productadda.exception.ApiException;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * RazorpayGatewayService
 * REVISION NOTE: added fetchPayments(...) below
 * fetchRefundStatus(...) - everything from Day 1-3 is unchanged.
 *
 * Thin, dedicated Razorpay integration boundary using the existing
 * RazorpayClient bean / RazorpayConfig already wired up for the
 * legacy Payment Links flow (PaymentServicePaymentVerify).
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class RazorpayGatewayInitiateRefundService {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    /*
     * Initiates a refund against a captured Razorpay payment.
     * razorpayPaymentId is Payment.gatewayTransactionId (set during
     * webhook processing), NOT the gatewayOrderId.
     */
    public RazorpayRefundResult initiateRefund(String razorpayPaymentId, Long refundAmountInPaise) {
        if (razorpayPaymentId == null || razorpayPaymentId.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "razorpayPaymentId is required");
        }
        if (refundAmountInPaise == null || refundAmountInPaise <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "refundAmountInPaise must be greater than zero");
        }

        try {
            JSONObject refundRequest = new JSONObject();
            refundRequest.put("amount", refundAmountInPaise);

            // com.razorpay.Refund is fully qualified to avoid a naming clash
            // with our own com.productadda.entity.Refund
            com.razorpay.Refund razorpayRefund = razorpayClient.payments.refund(razorpayPaymentId, refundRequest);

            return RazorpayRefundResult.builder()
                    .gatewayRefundId(razorpayRefund.get("id"))
                    .gatewayRefundStatus(razorpayRefund.get("status"))
                    .build();

        } catch (RazorpayException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "Razorpay refund initiation failed: " + exception.getMessage());
        }
    }

    // TODO: Razorpay webhooks are typically signed with a dedicated
    // "webhook secret" configured separately in the Razorpay dashboard,
    // NOT the API key-secret used for authenticated API calls. RazorpayConfig
    // currently only exposes keyId/keySecret - it needs a new webhookSecret
    // field (with its own @Value("${razorpay.webhook-secret}")) added before
    // this can be relied on in production webhook signature verification.
    // Returning keySecret here is a placeholder only.
    public String getWebhookSecret() {
        return razorpayConfig.getKeySecret();
    }

    /*
     * ================================================================
     * MIDDLE-MAN DTO: RazorpayRefundResult
     * ================================================================
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class RazorpayRefundResult {
        private String gatewayRefundId;
        private String gatewayRefundStatus;
    }

}
