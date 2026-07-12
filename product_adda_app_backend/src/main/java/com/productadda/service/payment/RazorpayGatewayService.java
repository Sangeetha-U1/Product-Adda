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
 * NEW SERVICE (Week 7): RazorpayGatewayService
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
}
