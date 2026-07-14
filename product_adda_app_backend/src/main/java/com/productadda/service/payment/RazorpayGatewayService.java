package com.productadda.service.payment;

import java.util.UUID;

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
 * SERVICE (Week 7): RazorpayGatewayService
 * REVISION NOTE (Day 3): added initiateRefund(...) and
 * fetchRefundStatus(...) below fetchOrderStatus(...) - everything
 * from Day 1 is unchanged.
 *
 * Thin, dedicated Razorpay integration boundary using the existing
 * RazorpayClient bean / RazorpayConfig already wired up for the
 * legacy Payment Links flow (PaymentServicePaymentVerify).
 * This uses the Razorpay Orders API (razorpayClient.orders), a
 * different Razorpay product surface than that legacy flow.
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class RazorpayGatewayService {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    /*
     * Creates a Razorpay Order for the given amount via the Orders API.
     * Returns the gateway order id and the frontend checkout key.
     */
    public RazorpayOrderResult createOrder(Long amountInPaise, UUID orderId) {
        if (amountInPaise == null || amountInPaise <= 0) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "amountInPaise must be greater than zero");
        }

        try {
            JSONObject orderRequest = new JSONObject();
            orderRequest.put("amount", amountInPaise);
            orderRequest.put("currency", "INR");
            orderRequest.put("receipt", orderId.toString());

            // com.razorpay.Order is fully qualified to avoid a naming clash
            // with our own com.productadda.entity.Order
            com.razorpay.Order razorpayOrder = razorpayClient.orders.create(orderRequest);
            String gatewayOrderId = razorpayOrder.get("id");

            return RazorpayOrderResult.builder()
                    .gatewayOrderId(gatewayOrderId)
                    .checkoutRedirectUrl("https://checkout.razorpay.com/v1/checkout.js?order_id=" + gatewayOrderId)
                    .checkoutKey(razorpayConfig.getKeyId())
                    .build();

        } catch (RazorpayException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Razorpay order creation failed: " + exception.getMessage());
        }
    }

    /*
     * Fetches the current status of a Razorpay order. Used by the
     * Day 2 payment verify / status lookup flow.
     */
    public String fetchOrderStatus(String gatewayOrderId) {
        if (gatewayOrderId == null || gatewayOrderId.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "gatewayOrderId is required");
        }

        try {
            com.razorpay.Order razorpayOrder = razorpayClient.orders.fetch(gatewayOrderId);
            return razorpayOrder.get("status");
        } catch (RazorpayException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Razorpay order fetch failed: " + exception.getMessage());
        }
    }

    /*
     * NEW (Day 3): Initiates a refund against a captured Razorpay payment.
     * razorpayPaymentId is Payment.gatewayTransactionId (set during webhook
     * processing on Day 1), NOT the gatewayOrderId.
     *
     * TODO: this call is written against the documented Razorpay Java SDK
     * surface (razorpayClient.payments.refund(paymentId, JSONObject)),
     * mirroring the confirmed razorpayClient.payments.fetch(paymentId)
     * usage in the legacy PaymentServicePaymentVerify.java. It has NOT
     * been verified against your exact installed razorpay-java version -
     * confirm the method signature compiles before deploying.
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
            throw new ApiException(HttpStatus.BAD_REQUEST, "Razorpay refund initiation failed: " + exception.getMessage());
        }
    }

    /*
     * NEW (Day 3): Fetches the current status of a previously-initiated
     * Razorpay refund. Used by the Get Refund Status endpoint's optional
     * live-sync path.
     */
    public String fetchRefundStatus(String gatewayRefundId) {
        if (gatewayRefundId == null || gatewayRefundId.trim().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "gatewayRefundId is required");
        }

        try {
            com.razorpay.Refund razorpayRefund = razorpayClient.refunds.fetch(gatewayRefundId);
            return razorpayRefund.get("status");
        } catch (RazorpayException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Razorpay refund fetch failed: " + exception.getMessage());
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
     * MIDDLE-MAN DTO: RazorpayOrderResult
     * Internal transfer object between this gateway service and the
     * calling business service. Not a REST-facing DTO.
     * ================================================================
     */
    @Getter
    @Builder
    @AllArgsConstructor
    public static class RazorpayOrderResult {
        private String gatewayOrderId;
        private String checkoutRedirectUrl;
        private String checkoutKey;
    }

    /*
     * ================================================================
     * MIDDLE-MAN DTO: RazorpayRefundResult
     * Internal transfer object for initiateRefund(...). Not a
     * REST-facing DTO.
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
