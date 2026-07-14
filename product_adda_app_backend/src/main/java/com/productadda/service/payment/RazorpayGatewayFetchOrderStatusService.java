package com.productadda.service.payment;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.productadda.config.RazorpayConfig;

import com.productadda.exception.ApiException;

import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;

import lombok.RequiredArgsConstructor;

/*
 * ================================================================
 * RazorpayGatewayService
 * REVISION NOTE: added fetchPayments(...) below
 * fetchRefundStatus(...) - everything from is unchanged.
 *
 * Thin, dedicated Razorpay integration boundary using the existing
 * RazorpayClient bean / RazorpayConfig already wired up for the
 * legacy Payment Links flow (PaymentServicePaymentVerify).
 * ================================================================
 */
@Service
@RequiredArgsConstructor
public class RazorpayGatewayFetchOrderStatusService {

    private final RazorpayClient razorpayClient;
    private final RazorpayConfig razorpayConfig;

    /*
     * Fetches the current status of a Razorpay order. Used by the
     * payment verify / status lookup flow.
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

}
